package com.truffleapp.truffle.data.db

import android.app.Application
import android.content.Context
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.Budget
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.data.BackupImportPreview
import com.truffleapp.truffle.data.DEFAULT_LEDGER_CURRENCY
import com.truffleapp.truffle.data.ImportBackupResult
import com.truffleapp.truffle.data.LEDGER_BACKUP_SCHEMA_VERSION
import com.truffleapp.truffle.data.LedgerData
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.data.parseBillRecurrence
import com.truffleapp.truffle.data.toPersistCode
import com.truffleapp.truffle.data.emptyLedgerData
import com.truffleapp.truffle.data.ledgerWithDerivedBudgetsAndWeekly
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * Room-backed ledger store plus one-time migration from legacy SharedPreferences JSON.
 */
class LedgerRepository(application: Application) {

    private val appContext = application.applicationContext
    private val prefs      = appContext.getSharedPreferences("ledger_prefs", Context.MODE_PRIVATE)
    private val dao        = LedgerDatabase.getInstance(appContext).ledgerDao()

    init {
        migrateLegacyPrefsIfNeeded()
        ensureMetaRow()
    }

    fun readHasOnboarded(): Boolean =
        dao.getMeta()?.hasOnboarded == true

    fun loadLedgerData(): LedgerData {
        val meta0 = dao.getMeta() ?: defaultMeta().also { dao.upsertMeta(it) }
        val base  = buildBaseLedgerFromRows(meta0)
            .rollUpTxTotals()
            .withSyncedNetWorth()
        val (rolled, newMeta) = applyNetWorthMonthRolloverPair(base, meta0)
        dao.upsertMeta(newMeta)
        return ledgerWithDerivedBudgetsAndWeekly(rolled)
    }

    fun persist(working: LedgerData): LedgerData {
        val meta0 = dao.getMeta() ?: defaultMeta()
        val normalized = working.rollUpTxTotals().withSyncedNetWorth()
        val (rolled, newMeta) = applyNetWorthMonthRolloverPair(normalized, meta0)
        val final = ledgerWithDerivedBudgetsAndWeekly(rolled)
        writeAll(
            ledger = final,
            meta   = newMeta.copy(
                userFirstName   = final.user.firstName,
                hasOnboarded    = meta0.hasOnboarded,
                displayCurrency = normalizeLedgerCurrencyCode(final.displayCurrency),
            ),
        )
        return final
    }

    fun completeOnboarding(userFirstName: String, firstAccount: Account) {
        val prior = dao.getMeta() ?: defaultMeta()
        val dc = normalizeLedgerCurrencyCode(firstAccount.currency)
        val meta0 = prior.copy(
            userFirstName    = userFirstName.trim(),
            hasOnboarded     = true,
            displayCurrency  = dc,
        )
        val ledger = emptyLedgerData(
            userName        = userFirstName.trim(),
            accounts        = listOf(firstAccount),
            displayCurrency = dc,
        )
            .rollUpTxTotals()
            .withSyncedNetWorth()
        val (rolled, newMeta) = applyNetWorthMonthRolloverPair(ledger, meta0)
        val final = ledgerWithDerivedBudgetsAndWeekly(rolled)
        writeAll(
            ledger = final,
            meta   = newMeta.copy(
                userFirstName   = final.user.firstName,
                hasOnboarded    = true,
                displayCurrency = normalizeLedgerCurrencyCode(final.displayCurrency),
            ),
        )
    }

    private fun buildBaseLedgerFromRows(meta: AppMetaEntity): LedgerData =
        emptyLedgerData(
            userName        = meta.userFirstName,
            accounts        = dao.listAccounts().map { it.toDomain() },
            displayCurrency = normalizeLedgerCurrencyCode(meta.displayCurrency),
        ).copy(
            transactions = dao.listTransactions().map { it.toDomain() },
            bills        = dao.listBills().map { it.toDomain() },
            goals        = dao.listGoals().map { it.toDomain() },
            budgets      = dao.listBudgets().map { it.toDomain() },
            weeklyFlow   = emptyList(),
        )

    private fun ensureMetaRow() {
        if (dao.getMeta() == null) dao.upsertMeta(defaultMeta())
    }

    private fun defaultMeta() = AppMetaEntity(
        id              = 1,
        userFirstName   = "",
        hasOnboarded    = false,
        nwSnapYm        = null,
        nwSnapNw        = null,
        nwBaseline      = "0.0",
        displayCurrency = DEFAULT_LEDGER_CURRENCY,
    )

    private fun applyNetWorthMonthRolloverPair(
        ledger: LedgerData,
        meta: AppMetaEntity,
    ): Pair<LedgerData, AppMetaEntity> {
        val nw    = ledger.netWorth
        val ymNow = YearMonth.now().toString()
        val snapYm = meta.nwSnapYm
        val baseline: Double
        val newMeta: AppMetaEntity
        when {
            snapYm.isNullOrEmpty() -> {
                baseline = 0.0
                newMeta  = meta.copy(
                    nwSnapYm   = ymNow,
                    nwSnapNw   = nw.toString(),
                    nwBaseline = "0.0",
                )
            }
            snapYm != ymNow -> {
                val previousClose = meta.nwSnapNw?.toDoubleOrNull() ?: nw
                baseline = previousClose
                newMeta  = meta.copy(
                    nwSnapYm   = ymNow,
                    nwSnapNw   = nw.toString(),
                    nwBaseline = previousClose.toString(),
                )
            }
            else -> {
                baseline = meta.nwBaseline?.toDoubleOrNull() ?: 0.0
                newMeta  = meta.copy(nwSnapNw = nw.toString())
            }
        }
        return Pair(ledger.copy(netWorthLastMonth = baseline), newMeta)
    }

    private fun writeAll(ledger: LedgerData, meta: AppMetaEntity) {
        dao.replaceAll(
            meta         = meta,
            accounts     = ledger.accounts.map { it.toEntity() },
            transactions = ledger.transactions.map { it.toEntity() },
            bills        = ledger.bills.map { it.toEntity() },
            goals        = ledger.goals.map { it.toEntity() },
            budgets      = ledger.budgets.map { it.toEntity() },
        )
    }

    private fun LedgerData.rollUpTxTotals(): LedgerData {
        val inf = transactions.sumOf { if (it.amount > 0) it.amount else 0.0 }
        val out = transactions.sumOf { if (it.amount < 0) -it.amount else 0.0 }
        return copy(inflow = inf, outflow = out)
    }

    private fun LedgerData.withSyncedNetWorth(): LedgerData =
        copy(netWorth = accounts.sumOf { it.balance })

    /**
     * Pretty-printed JSON snapshot of the Room ledger (for user backup / future restore tooling).
     */
    fun exportBackupJson(): String {
        val meta = dao.getMeta() ?: defaultMeta()
        val root = JSONObject()
        root.put("schema", LEDGER_BACKUP_SCHEMA_VERSION)
        root.put("exportedAt", Instant.now().toString())
        root.put("userFirstName", meta.userFirstName)
        root.put("hasOnboarded", meta.hasOnboarded)
        root.put("nwSnapYm", meta.nwSnapYm ?: JSONObject.NULL)
        root.put("nwSnapNw", meta.nwSnapNw ?: JSONObject.NULL)
        root.put("nwBaseline", meta.nwBaseline ?: "0.0")
        root.put("displayCurrency", meta.displayCurrency)

        root.put(
            "accounts",
            JSONArray().apply { dao.listAccounts().forEach { put(accountEntityToJson(it)) } },
        )
        root.put(
            "transactions",
            JSONArray().apply { dao.listTransactions().forEach { put(transactionEntityToJson(it)) } },
        )
        root.put(
            "bills",
            JSONArray().apply { dao.listBills().forEach { put(billEntityToJson(it)) } },
        )
        root.put(
            "goals",
            JSONArray().apply { dao.listGoals().forEach { put(goalEntityToJson(it)) } },
        )
        root.put(
            "budgets",
            JSONArray().apply { dao.listBudgets().forEach { put(budgetEntityToJson(it)) } },
        )
        return root.toString(2)
    }

    /** Lightweight parse for import confirmation UI (does not write to the database). */
    fun peekBackupPreview(json: String): BackupImportPreview? =
        try {
            val o = JSONObject(json.trim())
            BackupImportPreview(
                schema           = if (o.has("schema")) o.optInt("schema", 1) else 1,
                exportedAt       = o.optString("exportedAt", "").takeIf { it.isNotBlank() },
                accountCount     = o.optJSONArray("accounts")?.length() ?: 0,
                transactionCount = o.optJSONArray("transactions")?.length() ?: 0,
                billCount        = o.optJSONArray("bills")?.length() ?: 0,
                goalCount        = o.optJSONArray("goals")?.length() ?: 0,
                budgetCount      = o.optJSONArray("budgets")?.length() ?: 0,
            )
        } catch (_: Exception) {
            null
        }

    /**
     * Replaces the entire Room ledger from a JSON string produced by [exportBackupJson]
     * (schema 1). On success, the next [loadLedgerData] reflects imported rows and meta.
     */
    fun importBackupJson(json: String): ImportBackupResult {
        return try {
            val root = JSONObject(json.trim())
            val schema = if (root.has("schema")) root.optInt("schema", 1) else 1
            if (schema > LEDGER_BACKUP_SCHEMA_VERSION) {
                return ImportBackupResult.Failure("This backup needs a newer version of the app.")
            }
            val meta = parseBackupMetaFromRoot(root)
            val accounts = root.optJSONArray("accounts")?.mapJsonObjects { it.toAccountEntity() } ?: emptyList()
            val transactions = root.optJSONArray("transactions")?.mapJsonObjects { it.toTransactionEntity() }
                ?: emptyList()
            val bills = root.optJSONArray("bills")?.mapJsonObjects { it.toBillEntity() } ?: emptyList()
            val goals = root.optJSONArray("goals")?.mapJsonObjects { it.toGoalEntity() } ?: emptyList()
            val budgets = root.optJSONArray("budgets")?.mapJsonObjects { it.toBudgetEntity() } ?: emptyList()
            dao.replaceAll(
                meta         = meta,
                accounts     = accounts,
                transactions = transactions,
                bills        = bills,
                goals        = goals,
                budgets      = budgets,
            )
            ImportBackupResult.Success
        } catch (e: Exception) {
            ImportBackupResult.Failure(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Could not read this file as a Truffle backup.",
            )
        }
    }

    /** Wipes all ledger rows and onboarding state. User must complete onboarding again. */
    fun clearAllDataAndResetOnboarding() {
        dao.replaceAll(
            meta         = defaultMeta(),
            accounts     = emptyList(),
            transactions = emptyList(),
            bills        = emptyList(),
            goals        = emptyList(),
            budgets      = emptyList(),
        )
    }

    private fun accountEntityToJson(a: AccountEntity) = JSONObject().apply {
        put("id", a.id)
        put("name", a.name)
        put("institution", a.institution)
        put("balance", a.balance)
        put("kind", a.kind)
        put("currency", a.currency)
        put("creditLimit", a.creditLimit)
    }

    private fun transactionEntityToJson(t: TransactionEntity) = JSONObject().apply {
        put("id", t.id)
        put("date", t.date)
        put("time", t.time)
        put("merchant", t.merchant)
        put("note", t.note)
        put("amount", t.amount)
        put("category", t.category)
        put("icon", t.icon)
        put("account", t.account)
        put("recordedEpochDay", t.recordedEpochDay)
    }

    private fun billEntityToJson(b: BillEntity) = JSONObject().apply {
        put("id", b.id)
        put("label", b.label)
        put("amount", b.amount)
        put("dueDateEpoch", b.dueDateEpoch)
        put("paid", b.paid)
        put("account", b.account)
        put("recurrence", b.recurrence)
    }

    private fun goalEntityToJson(g: GoalEntity) = JSONObject().apply {
        put("id", g.id)
        put("title", g.title)
        put("note", g.note)
        put("saved", g.saved)
        put("target", g.target)
    }

    private fun budgetEntityToJson(b: BudgetEntity) = JSONObject().apply {
        put("id", b.id)
        put("label", b.label)
        put("icon", b.icon)
        put("spent", b.spent)
        put("limit", b.limit)
    }

    private fun parseBackupMetaFromRoot(root: JSONObject) = AppMetaEntity(
        id              = 1,
        userFirstName   = root.optString("userFirstName", ""),
        hasOnboarded    = root.optBoolean("hasOnboarded", false),
        nwSnapYm        = if (!root.has("nwSnapYm") || root.isNull("nwSnapYm")) null else root.getString("nwSnapYm"),
        nwSnapNw        = if (!root.has("nwSnapNw") || root.isNull("nwSnapNw")) null else root.getString("nwSnapNw"),
        nwBaseline      = when {
            !root.has("nwBaseline") || root.isNull("nwBaseline") -> "0.0"
            else -> root.optString("nwBaseline", "0.0")
        },
        displayCurrency = normalizeLedgerCurrencyCode(
            root.optString("displayCurrency", DEFAULT_LEDGER_CURRENCY),
        ),
    )

    private fun migrateLegacyPrefsIfNeeded() {
        if (prefs.getBoolean(KEY_ROOM_MIGRATED, false)) return

        val hadPrefsData = prefs.contains("accounts") ||
            prefs.contains(KEY_TRANSACTIONS) ||
            prefs.getBoolean("has_onboarded", false)

        if (!hadPrefsData) {
            prefs.edit().putBoolean(KEY_ROOM_MIGRATED, true).apply()
            return
        }

        val meta = AppMetaEntity(
            id              = 1,
            userFirstName   = prefs.getString("user_name", "") ?: "",
            hasOnboarded    = prefs.getBoolean("has_onboarded", false),
            nwSnapYm        = prefs.getString("nw_snap_ym", null),
            nwSnapNw        = prefs.getString("nw_snap_nw", null),
            nwBaseline      = prefs.getString("nw_baseline", null) ?: "0.0",
            displayCurrency = DEFAULT_LEDGER_CURRENCY,
        )

        dao.replaceAll(
            meta         = meta,
            accounts     = parseAccountsJson(prefs.getString("accounts", "[]") ?: "[]"),
            transactions = parseTransactionsJson(prefs.getString(KEY_TRANSACTIONS, null)),
            bills        = parseBillsJson(prefs.getString(KEY_BILLS, null)),
            goals        = parseGoalsJson(prefs.getString(KEY_GOALS, null)),
            budgets      = parseBudgetsJson(prefs.getString(KEY_BUDGETS, null)),
        )
        prefs.edit().putBoolean(KEY_ROOM_MIGRATED, true).apply()
    }

    private companion object {
        const val KEY_ROOM_MIGRATED = "ledger_room_migrated_v1"
        const val KEY_TRANSACTIONS = "transactions"
        const val KEY_BILLS        = "bills"
        const val KEY_GOALS        = "goals"
        const val KEY_BUDGETS      = "budgets"
    }
}

// ── Legacy JSON parsing ───────────────────────────────────────────────────

private fun <T> JSONArray.mapJsonObjects(mapper: (JSONObject) -> T): List<T> =
    (0 until length()).map { i -> mapper(getJSONObject(i)) }

private fun JSONObject.toAccountEntity(): AccountEntity {
    val kind = getString("kind").also { AccountKind.valueOf(it) }
    return AccountEntity(
        id          = getString("id"),
        name        = getString("name"),
        institution = optString("institution", ""),
        balance     = getDouble("balance"),
        kind        = kind,
        currency    = normalizeLedgerCurrencyCode(optString("currency", DEFAULT_LEDGER_CURRENCY)),
        creditLimit = optDouble("creditLimit", 0.0).coerceAtLeast(0.0),
    )
}

private fun JSONObject.toTransactionEntity(): TransactionEntity = TransactionEntity(
    id               = getString("id"),
    date             = getString("date"),
    time             = getString("time"),
    merchant         = getString("merchant"),
    note             = getString("note"),
    amount           = getDouble("amount"),
    category         = getString("category"),
    icon             = getString("icon"),
    account          = getString("account"),
    recordedEpochDay = optLong("recordedEpochDay", 0L),
)

private fun JSONObject.toBillEntity(): BillEntity {
    val dueEpoch = when {
        has("dueDateEpoch") && !isNull("dueDateEpoch") ->
            getLong("dueDateEpoch")
        else -> {
            val dueIn = when {
                has("dueIn") && !isNull("dueIn") -> getInt("dueIn")
                has("due_in") && !isNull("due_in") -> getInt("due_in")
                else -> 7
            }
            LocalDate.now().plusDays(dueIn.toLong()).toEpochDay()
        }
    }
    return BillEntity(
        id           = getString("id"),
        label        = getString("label"),
        amount       = getDouble("amount"),
        dueDateEpoch = dueEpoch,
        paid         = optBoolean("paid", false),
        account      = getString("account"),
        recurrence   = parseBillRecurrence(optString("recurrence", "NONE")).toPersistCode(),
    )
}

private fun JSONObject.toGoalEntity(): GoalEntity = GoalEntity(
    id     = getString("id"),
    title  = getString("title"),
    note   = getString("note"),
    saved  = getDouble("saved"),
    target = getDouble("target"),
)

private fun JSONObject.toBudgetEntity(): BudgetEntity = BudgetEntity(
    id    = getString("id"),
    label = getString("label"),
    icon  = getString("icon"),
    spent = getDouble("spent"),
    limit = getDouble("limit"),
)

private fun parseAccountsJson(json: String): List<AccountEntity> = try {
    JSONArray(json).mapJsonObjects { it.toAccountEntity() }
} catch (_: Exception) { emptyList() }

private fun parseTransactionsJson(json: String?): List<TransactionEntity> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        JSONArray(json).mapJsonObjects { it.toTransactionEntity() }
    } catch (_: Exception) { emptyList() }
}

private fun parseBillsJson(json: String?): List<BillEntity> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        JSONArray(json).mapJsonObjects { it.toBillEntity() }
    } catch (_: Exception) { emptyList() }
}

private fun parseGoalsJson(json: String?): List<GoalEntity> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        JSONArray(json).mapJsonObjects { it.toGoalEntity() }
    } catch (_: Exception) { emptyList() }
}

private fun parseBudgetsJson(json: String?): List<BudgetEntity> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        JSONArray(json).mapJsonObjects { it.toBudgetEntity() }
    } catch (_: Exception) { emptyList() }
}

// ── Mappers ────────────────────────────────────────────────────────────────

private fun AccountEntity.toDomain() = Account(
    id           = id,
    name         = name,
    institution  = institution,
    balance      = balance,
    kind         = AccountKind.valueOf(kind),
    currency     = normalizeLedgerCurrencyCode(currency),
    creditLimit  = creditLimit.coerceAtLeast(0.0),
)

private fun Account.toEntity() = AccountEntity(
    id           = id,
    name         = name,
    institution  = institution,
    balance      = balance,
    kind         = kind.name,
    currency     = normalizeLedgerCurrencyCode(currency),
    creditLimit  = creditLimit.coerceAtLeast(0.0),
)

private fun TransactionEntity.toDomain() = Transaction(
    id                = id,
    date              = date,
    time              = time,
    merchant          = merchant,
    note              = note,
    amount            = amount,
    category          = category,
    icon              = icon,
    account           = account,
    recordedEpochDay  = recordedEpochDay,
)

private fun Transaction.toEntity() = TransactionEntity(
    id                = id,
    date              = date,
    time              = time,
    merchant          = merchant,
    note              = note,
    amount            = amount,
    category          = category,
    icon              = icon,
    account           = account,
    recordedEpochDay  = recordedEpochDay,
)

private fun BillEntity.toDomain() = Bill(
    id            = id,
    label         = label,
    amount        = amount,
    dueDateEpoch  = dueDateEpoch,
    paid          = paid,
    account       = account,
    recurrence    = parseBillRecurrence(recurrence),
)

private fun Bill.toEntity() = BillEntity(
    id            = id,
    label         = label,
    amount        = amount,
    dueDateEpoch  = dueDateEpoch,
    paid          = paid,
    account       = account,
    recurrence    = recurrence.toPersistCode(),
)

private fun GoalEntity.toDomain() = Goal(
    id     = id,
    title  = title,
    note   = note,
    saved  = saved,
    target = target,
)

private fun Goal.toEntity() = GoalEntity(
    id     = id,
    title  = title,
    note   = note,
    saved  = saved,
    target = target,
)

private fun BudgetEntity.toDomain() = Budget(
    id    = id,
    label = label,
    icon  = icon,
    spent = spent,
    limit = limit,
)

private fun Budget.toEntity() = BudgetEntity(
    id    = id,
    label = label,
    icon  = icon,
    spent = spent,
    limit = limit,
)
