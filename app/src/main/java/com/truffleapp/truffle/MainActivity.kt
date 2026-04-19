package com.truffleapp.truffle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.data.BackupImportPreview
import com.truffleapp.truffle.data.ImportBackupResult
import com.truffleapp.truffle.data.LEDGER_BACKUP_SCHEMA_VERSION
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.navigation.NavDestination
import com.truffleapp.truffle.ui.components.AddToGoalSheet
import com.truffleapp.truffle.ui.components.AddTransactionSheet
import com.truffleapp.truffle.ui.components.AddTypeSheet
import com.truffleapp.truffle.ui.components.BillSheet
import com.truffleapp.truffle.ui.components.EditAccountSheet
import com.truffleapp.truffle.ui.components.BottomNavBar
import com.truffleapp.truffle.ui.components.NewAccountSheet
import com.truffleapp.truffle.ui.components.NewBillSheet
import com.truffleapp.truffle.ui.components.NewGoalSheet
import com.truffleapp.truffle.ui.components.TxDetailSheet
import com.truffleapp.truffle.ui.screens.AccountsScreen
import com.truffleapp.truffle.ui.screens.FlowScreen
import com.truffleapp.truffle.ui.screens.GoalsScreen
import com.truffleapp.truffle.ui.screens.OnboardingScreen
import com.truffleapp.truffle.ui.screens.TodayScreen
import com.truffleapp.truffle.ui.screens.TruffleSplash
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme
import com.truffleapp.truffle.ui.viewmodel.LedgerViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StillwaterTheme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    TruffleSplash(onFinished = { showSplash = false })
                } else {
                    LedgerApp()
                }
            }
        }
    }
}

@Composable
private fun LedgerApp() {
    val viewModel: LedgerViewModel = viewModel()
    val data = viewModel.data

    if (!viewModel.hasOnboarded) {
        OnboardingScreen(
            onComplete = { name, account -> viewModel.completeOnboarding(name, account) },
        )
        return
    }

    var currentDestination by rememberSaveable { mutableStateOf(NavDestination.Today) }

    // ── Detail sheet state ─────────────────────────────────────────────────
    var selectedTx   by remember { mutableStateOf<Transaction?>(null) }
    var selectedBill by remember { mutableStateOf<Bill?>(null) }
    var selectedGoal by remember { mutableStateOf<Goal?>(null) }

    // ── Add sheet state ────────────────────────────────────────────────────
    var showAddPicker      by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }
    var showAddBill        by remember { mutableStateOf(false) }
    var showAddGoal        by remember { mutableStateOf(false) }
    var showAddAccount     by remember { mutableStateOf(false) }
    var accountToEdit      by remember { mutableStateOf<Account?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<PendingImport?>(null) }

    val appContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                val text = appContext.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                }
                val fileLabel = resolveBackupDisplayName(appContext, uri)
                withContext(Dispatchers.Main) {
                    if (text.isNullOrBlank()) {
                        Toast.makeText(appContext, "That file was empty.", Toast.LENGTH_LONG).show()
                        return@withContext
                    }
                    val preview = viewModel.peekBackupPreview(text)
                    if (preview == null) {
                        Toast.makeText(
                            appContext,
                            "This file does not look like a Truffle backup.",
                            Toast.LENGTH_LONG,
                        ).show()
                        return@withContext
                    }
                    if (preview.schema > LEDGER_BACKUP_SCHEMA_VERSION) {
                        Toast.makeText(
                            appContext,
                            "This backup needs a newer version of the app.",
                            Toast.LENGTH_LONG,
                        ).show()
                        return@withContext
                    }
                    pendingImport = PendingImport(
                        json       = text,
                        fileLabel  = fileLabel,
                        preview    = preview,
                    )
                }
            }
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPage),
    ) {
        when (currentDestination) {
            NavDestination.Today -> TodayScreen(
                data   = data,
                onTx   = { selectedTx = it },
                onBill = { selectedBill = it },
                onNav  = { currentDestination = it },
                onAdd  = { showAddPicker = true },
            )
            NavDestination.Accounts -> AccountsScreen(
                data                   = data,
                onEditAccount          = { accountToEdit = it },
                onExportBackup         = { shareLedgerBackup(appContext, viewModel.exportBackupJson()) },
                onImportBackup         = { importBackupLauncher.launch("*/*") },
                onRequestClearAllData = { showClearAllConfirm = true },
            )
            NavDestination.Flow     -> FlowScreen(data = data, onTx = { selectedTx = it })
            NavDestination.Goals    -> GoalsScreen(data = data, onAddToGoal = { selectedGoal = it })
        }

        BottomNavBar(
            current = currentDestination,
            onNav   = { currentDestination = it },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }

    // ── Detail sheets ──────────────────────────────────────────────────────

    selectedTx?.let { tx ->
        TxDetailSheet(
            tx = tx,
            onDismiss = { selectedTx = null },
            onRecategorize = { txId, cat -> viewModel.recategorize(txId, cat) },
            onRemove = { txId ->
                viewModel.removeTransaction(txId)
                selectedTx = null
            },
        )
    }

    selectedBill?.let { bill ->
        BillSheet(
            bill = bill,
            onDismiss = { selectedBill = null },
            onMarkPaid = { billId ->
                viewModel.markBillPaid(billId)
                selectedBill = null
            },
        )
    }

    selectedGoal?.let { goal ->
        AddToGoalSheet(
            goal = goal,
            onDismiss = { selectedGoal = null },
            onConfirm = { goalId, amount ->
                viewModel.addToGoal(goalId, amount)
                selectedGoal = null
            },
        )
    }

    // ── Add sheets ─────────────────────────────────────────────────────────

    if (showAddPicker) {
        AddTypeSheet(
            onTransaction = { showAddPicker = false; showAddTransaction = true },
            onBill        = { showAddPicker = false; showAddBill = true },
            onGoal        = { showAddPicker = false; showAddGoal = true },
            onAccount     = { showAddPicker = false; showAddAccount = true },
            onDismiss     = { showAddPicker = false },
        )
    }

    if (showAddTransaction) {
        AddTransactionSheet(
            accounts  = data.accounts,
            onDismiss = { showAddTransaction = false },
            onAdd     = { tx ->
                viewModel.addTransaction(tx)
                showAddTransaction = false
            },
        )
    }

    if (showAddBill) {
        NewBillSheet(
            accounts  = data.accounts,
            onDismiss = { showAddBill = false },
            onAdd     = { bill ->
                viewModel.addBill(bill)
                showAddBill = false
            },
        )
    }

    if (showAddGoal) {
        NewGoalSheet(
            onDismiss = { showAddGoal = false },
            onAdd     = { goal ->
                viewModel.addGoal(goal)
                showAddGoal = false
            },
        )
    }

    if (showAddAccount) {
        NewAccountSheet(
            onDismiss = { showAddAccount = false },
            onAdd     = { account ->
                viewModel.addAccount(account)
                showAddAccount = false
            },
        )
    }

    accountToEdit?.let { editing ->
        val nameKey = editing.name.trim()
        val linkedTxCount = data.transactions.count { it.account.trim() == nameKey }
        val linkedBillCount = data.bills.count { it.account.trim() == nameKey }
        EditAccountSheet(
            account                  = editing,
            linkedTransactionCount   = linkedTxCount,
            linkedBillCount          = linkedBillCount,
            onDismiss                = { accountToEdit = null },
            onSave                   = { updated ->
                viewModel.updateAccount(updated)
                accountToEdit = null
            },
            onDelete                 = {
                viewModel.deleteAccount(editing.id)
                accountToEdit = null
            },
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = {
                Text(
                    text = "Clear all data?",
                    style = TextStyle(fontFamily = SerifFamily, fontSize = 18.sp, color = ColorInk),
                )
            },
            text = {
                Text(
                    text = "Every account, transaction, bill, goal, and budget will be erased from this device. Export a backup first if you want a copy. You will set the app up again from the beginning.",
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 14.sp,
                        color      = ColorTextSecondary,
                        lineHeight = (14 * 1.55).sp,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllConfirm = false
                        selectedTx = null
                        selectedBill = null
                        selectedGoal = null
                        accountToEdit = null
                        showAddPicker = false
                        showAddTransaction = false
                        showAddBill = false
                        showAddGoal = false
                        showAddAccount = false
                        viewModel.clearAllData()
                    },
                ) {
                    Text("Clear everything", style = TextStyle(fontFamily = SansFamily, color = ColorInk))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Cancel", style = TextStyle(fontFamily = SansFamily, color = ColorTextTertiary))
                }
            },
        )
    }

    pendingImport?.let { pending ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = {
                Text(
                    text = "Import this backup?",
                    style = TextStyle(fontFamily = SerifFamily, fontSize = 18.sp, color = ColorInk),
                )
            },
            text = {
                Column {
                    Text(
                        text = "File: ${pending.fileLabel}",
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontSize   = 14.sp,
                            color      = ColorInk,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = pending.preview.summaryText(),
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontSize   = 13.sp,
                            color      = ColorTextSecondary,
                            lineHeight = (13 * 1.45).sp,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "This device’s ledger will be replaced completely. Export first if you need a copy of what you have now. This cannot be undone.",
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontStyle  = FontStyle.Italic,
                            fontSize   = 14.sp,
                            color      = ColorTextSecondary,
                            lineHeight = (14 * 1.55).sp,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val json = pending.json
                        pendingImport = null
                        when (val r = viewModel.importBackupJson(json)) {
                            is ImportBackupResult.Success ->
                                Toast.makeText(appContext, "Backup restored.", Toast.LENGTH_SHORT).show()
                            is ImportBackupResult.Failure ->
                                Toast.makeText(appContext, r.message, Toast.LENGTH_LONG).show()
                        }
                    },
                ) {
                    Text("Replace ledger", style = TextStyle(fontFamily = SansFamily, color = ColorInk))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text("Cancel", style = TextStyle(fontFamily = SansFamily, color = ColorTextTertiary))
                }
            },
        )
    }
}

private data class PendingImport(
    val json: String,
    val fileLabel: String,
    val preview: BackupImportPreview,
)

private fun resolveBackupDisplayName(context: Context, uri: Uri): String {
    val fallback = uri.lastPathSegment
        ?.substringAfterLast(':')
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }
        ?: "Selected file"
    return context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { c ->
        if (!c.moveToFirst()) return@use null
        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx < 0) null else c.getString(idx)
    }?.takeIf { !it.isNullOrBlank() } ?: fallback
}

private fun shareLedgerBackup(context: Context, json: String) {
    runCatching {
        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val file = File(dir, "truffle_backup_${System.currentTimeMillis()}.json")
        file.writeText(json, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "Truffle ledger backup")
        }
        context.startActivity(Intent.createChooser(send, "Export backup"))
    }
}
