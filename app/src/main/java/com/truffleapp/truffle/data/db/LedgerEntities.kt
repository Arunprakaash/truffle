package com.truffleapp.truffle.data.db

import com.truffleapp.truffle.data.DEFAULT_LEDGER_CURRENCY
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "user_first_name") val userFirstName: String,
    @ColumnInfo(name = "has_onboarded") val hasOnboarded: Boolean,
    @ColumnInfo(name = "nw_snap_ym") val nwSnapYm: String?,
    @ColumnInfo(name = "nw_snap_nw") val nwSnapNw: String?,
    @ColumnInfo(name = "nw_baseline") val nwBaseline: String?,
    /** ISO 4217 — goals, budgets, and header totals. */
    @ColumnInfo(name = "display_currency") val displayCurrency: String = DEFAULT_LEDGER_CURRENCY,
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val institution: String,
    val balance: Double,
    val kind: String,
    /** ISO 4217 */
    val currency: String = DEFAULT_LEDGER_CURRENCY,
    /** Credit: max owed; 0 = not enforced. */
    @ColumnInfo(name = "credit_limit") val creditLimit: Double = 0.0,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val date: String,
    val time: String,
    val merchant: String,
    val note: String,
    val amount: Double,
    val category: String,
    val icon: String,
    val account: String,
    @ColumnInfo(name = "recorded_epoch_day") val recordedEpochDay: Long,
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String,
    val label: String,
    val amount: Double,
    @ColumnInfo(name = "due_date_epoch") val dueDateEpoch: Long,
    val paid: Boolean,
    val account: String,
    /** NONE, WEEKLY, MONTHLY, YEARLY — see [com.truffleapp.truffle.data.BillRecurrence]. */
    val recurrence: String = "NONE",
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val note: String,
    val saved: Double,
    val target: Double,
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val label: String,
    val icon: String,
    val spent: Double,
    val limit: Double,
)
