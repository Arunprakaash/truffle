package com.truffleapp.truffle.data

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** ISO 4217 code used when none is stored (backups, migration). */
const val DEFAULT_LEDGER_CURRENCY: String = "USD"

data class LedgerCurrencyOption(val code: String, val label: String)

/** Curated list for pickers (extend as needed). */
val LEDGER_CURRENCY_OPTIONS: List<LedgerCurrencyOption> = listOf(
    LedgerCurrencyOption("USD", "US dollar"),
    LedgerCurrencyOption("EUR", "Euro"),
    LedgerCurrencyOption("GBP", "British pound"),
    LedgerCurrencyOption("CAD", "Canadian dollar"),
    LedgerCurrencyOption("AUD", "Australian dollar"),
    LedgerCurrencyOption("CHF", "Swiss franc"),
    LedgerCurrencyOption("JPY", "Japanese yen"),
    LedgerCurrencyOption("INR", "Indian rupee"),
    LedgerCurrencyOption("CNY", "Chinese yuan"),
    LedgerCurrencyOption("MXN", "Mexican peso"),
    LedgerCurrencyOption("BRL", "Brazilian real"),
    LedgerCurrencyOption("SEK", "Swedish krona"),
    LedgerCurrencyOption("NOK", "Norwegian krone"),
    LedgerCurrencyOption("DKK", "Danish krone"),
    LedgerCurrencyOption("PLN", "Polish złoty"),
    LedgerCurrencyOption("NZD", "New Zealand dollar"),
    LedgerCurrencyOption("SGD", "Singapore dollar"),
    LedgerCurrencyOption("HKD", "Hong Kong dollar"),
    LedgerCurrencyOption("KRW", "South Korean won"),
    LedgerCurrencyOption("ZAR", "South African rand"),
)

private val allowedCodes: Set<String> = LEDGER_CURRENCY_OPTIONS.map { it.code }.toSet()

fun normalizeLedgerCurrencyCode(raw: String): String {
    val c = raw.trim().uppercase()
    return if (c in allowedCodes) c else DEFAULT_LEDGER_CURRENCY
}

/** Short symbol for UI prefixes (e.g. amount entry row). */
fun ledgerCurrencySymbol(code: String): String =
    try {
        Currency.getInstance(normalizeLedgerCurrencyCode(code)).symbol
    } catch (_: Exception) {
        Currency.getInstance(DEFAULT_LEDGER_CURRENCY).symbol
    }

/**
 * Formats [amount] in [currencyCode] (ISO 4217). Uses device locale for separators.
 * [sign] adds a leading +/− before the currency amount (detail views).
 */
fun formatLedgerMoney(
    amount: Double,
    currencyCode: String,
    cents: Boolean = false,
    sign: Boolean = false,
): String {
    val code = normalizeLedgerCurrencyCode(currencyCode)
    val currency = try {
        Currency.getInstance(code)
    } catch (_: IllegalArgumentException) {
        Currency.getInstance(DEFAULT_LEDGER_CURRENCY)
    }
    val nf = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        this.currency = currency
        val digits = if (cents) currency.defaultFractionDigits.coerceIn(0, 2) else 0
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    val abs = kotlin.math.abs(amount)
    val formatted = nf.format(abs)
    return when {
        sign -> "${if (amount >= 0) "+ " else "\u2212 "}$formatted"
        amount < 0 -> "\u2212 $formatted"
        else -> formatted
    }
}

/** Currency for amounts tied to [accountName] (tx, bills); unassigned uses [LedgerData.displayCurrency]. */
fun LedgerData.currencyForAccountName(accountName: String): String {
    val key = accountName.trim()
    if (key.isEmpty() || key == UNASSIGNED_ACCOUNT_LABEL) return displayCurrency
    return accounts.find { it.name.trim() == key }?.let { normalizeLedgerCurrencyCode(it.currency) }
        ?: displayCurrency
}

/**
 * Net worth and other “whole ledger” totals: with a single account, match that account’s
 * [Account.currency] so it stays consistent with Edit account; otherwise [LedgerData.displayCurrency].
 */
fun LedgerData.primaryAmountCurrency(): String =
    if (accounts.size == 1) normalizeLedgerCurrencyCode(accounts[0].currency)
    else displayCurrency
