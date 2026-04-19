package com.truffleapp.truffle.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface LedgerDao {

    @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
    fun getMeta(): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertMeta(meta: AppMetaEntity)

    @Query("SELECT * FROM accounts ORDER BY name COLLATE NOCASE ASC")
    fun listAccounts(): List<AccountEntity>

    @Query("SELECT * FROM transactions ORDER BY recorded_epoch_day DESC, id DESC")
    fun listTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM bills ORDER BY due_date_epoch ASC, id ASC")
    fun listBills(): List<BillEntity>

    @Query("SELECT * FROM goals")
    fun listGoals(): List<GoalEntity>

    @Query("SELECT * FROM budgets")
    fun listBudgets(): List<BudgetEntity>

    @Query("DELETE FROM accounts")
    fun deleteAllAccounts()

    @Query("DELETE FROM transactions")
    fun deleteAllTransactions()

    @Query("DELETE FROM bills")
    fun deleteAllBills()

    @Query("DELETE FROM goals")
    fun deleteAllGoals()

    @Query("DELETE FROM budgets")
    fun deleteAllBudgets()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccounts(rows: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactions(rows: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBills(rows: List<BillEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGoals(rows: List<GoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudgets(rows: List<BudgetEntity>)

    @Transaction
    fun replaceAll(
        meta: AppMetaEntity,
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>,
        bills: List<BillEntity>,
        goals: List<GoalEntity>,
        budgets: List<BudgetEntity>,
    ) {
        deleteAllAccounts()
        deleteAllTransactions()
        deleteAllBills()
        deleteAllGoals()
        deleteAllBudgets()
        upsertMeta(meta)
        if (accounts.isNotEmpty()) insertAccounts(accounts)
        if (transactions.isNotEmpty()) insertTransactions(transactions)
        if (bills.isNotEmpty()) insertBills(bills)
        if (goals.isNotEmpty()) insertGoals(goals)
        if (budgets.isNotEmpty()) insertBudgets(budgets)
    }
}
