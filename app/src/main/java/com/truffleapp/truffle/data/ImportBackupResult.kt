package com.truffleapp.truffle.data

/** Current JSON backup format written by [com.truffleapp.truffle.data.db.LedgerRepository.exportBackupJson]. */
const val LEDGER_BACKUP_SCHEMA_VERSION = 1

data class BackupImportPreview(
    val schema: Int,
    val exportedAt: String?,
    val accountCount: Int,
    val transactionCount: Int,
    val billCount: Int,
    val goalCount: Int,
    val budgetCount: Int,
) {
    fun summaryText(): String = buildString {
        append("Format v$schema")
        exportedAt?.let { append(" · exported ").append(it) }
        append("\n")
        append("$accountCount accounts · $transactionCount transactions · $billCount bills")
        append("\n")
        append("$goalCount goals · $budgetCount budgets")
    }
}

sealed class ImportBackupResult {
    data object Success : ImportBackupResult()
    data class Failure(val message: String) : ImportBackupResult()
}
