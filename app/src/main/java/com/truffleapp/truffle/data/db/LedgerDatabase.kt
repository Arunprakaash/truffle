package com.truffleapp.truffle.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

@Database(
    entities = [
        AppMetaEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        BillEntity::class,
        GoalEntity::class,
        BudgetEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class LedgerDatabase : RoomDatabase() {

    abstract fun ledgerDao(): LedgerDao

    companion object {
        @Volatile
        private var instance: LedgerDatabase? = null

        fun getInstance(context: Context): LedgerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LedgerDatabase::class.java,
                    "truffle_ledger.db",
                )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bills_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        label TEXT NOT NULL,
                        amount REAL NOT NULL,
                        due_date_epoch INTEGER NOT NULL,
                        paid INTEGER NOT NULL,
                        account TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                val anchor = LocalDate.now()
                val c = db.query("SELECT id, label, amount, due_in, paid, account FROM bills")
                try {
                    while (c.moveToNext()) {
                        val id = c.getString(0)
                        val label = c.getString(1)
                        val amount = c.getDouble(2)
                        val dueIn = c.getInt(3)
                        val paid = c.getInt(4)
                        val account = c.getString(5)
                        val dueEpoch = anchor.plusDays(dueIn.toLong()).toEpochDay()
                        db.execSQL(
                            "INSERT INTO bills_new (id, label, amount, due_date_epoch, paid, account) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf<Any>(id, label, amount, dueEpoch, paid, account),
                        )
                    }
                } finally {
                    c.close()
                }
                db.execSQL("DROP TABLE bills")
                db.execSQL("ALTER TABLE bills_new RENAME TO bills")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE app_meta ADD COLUMN display_currency TEXT NOT NULL DEFAULT 'USD'",
                )
                db.execSQL(
                    "ALTER TABLE accounts ADD COLUMN currency TEXT NOT NULL DEFAULT 'USD'",
                )
            }
        }
    }
}
