package com.truffleapp.truffle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.data.LedgerData
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.ui.components.AccountBackupSheet
import com.truffleapp.truffle.ui.components.BottomNavContentPadding
import com.truffleapp.truffle.ui.components.AccountRow
import com.truffleapp.truffle.ui.components.Caps
import com.truffleapp.truffle.ui.components.MoneyText
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme

private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun AccountsScreen(
    data: LedgerData,
    modifier: Modifier = Modifier,
    onEditAccount: (Account) -> Unit = {},
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onRequestClearAllData: () -> Unit = {},
    onDisplayCurrencyChange: (String) -> Unit = {},
) {
    val cash    = remember(data) { data.accounts.filter { it.kind == AccountKind.Cash } }
    val invest  = remember(data) { data.accounts.filter { it.kind == AccountKind.Invest } }
    val credit  = remember(data) { data.accounts.filter { it.kind == AccountKind.Credit } }

    val cashTotal   = remember(cash)   { cash.sumOf { it.balance } }
    val investTotal = remember(invest) { invest.sumOf { it.balance } }
    val creditTotal = remember(credit) { credit.sumOf { it.balance } }

    val institutionCount = remember(data) {
        data.accounts.map { it.institution }.distinct().size
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = BottomNavContentPadding),
    ) {
        // ── Summary header ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 12.dp, bottom = 4.dp),
        ) {
            var showBackupSheet by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Caps(text = "Held in total", modifier = Modifier.weight(1f))
                IconButton(onClick = { showBackupSheet = true }) {
                    Icon(
                        imageVector      = Icons.Outlined.MoreVert,
                        contentDescription = "Data and backup",
                        tint               = ColorInk,
                    )
                }
            }

            if (showBackupSheet) {
                AccountBackupSheet(
                    displayCurrency         = data.displayCurrency,
                    onDisplayCurrencyChange = onDisplayCurrencyChange,
                    onDismiss               = { showBackupSheet = false },
                    onImport                = onImportBackup,
                    onExport                = onExportBackup,
                    onRequestClearAll       = onRequestClearAllData,
                )
            }

            Spacer(Modifier.height(10.dp))

            MoneyText(
                amount = data.netWorth,
                currencyCode = data.displayCurrency,
                size = 36.sp,
            )

            Text(
                text = "Across ${data.accounts.size} accounts, " +
                       "at $institutionCount institutions",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 12.sp,
                    color      = ColorTextSerifMuted,
                ),
                modifier = Modifier.padding(top = 6.dp),
            )

            Text(
                text = "Tap or hold an account to edit.",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 12.sp,
                    color      = ColorTextSerifBody,
                ),
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        // ── Account groups ────────────────────────────────────────────────
        AccountGroup(
            title           = "Cash",
            total           = cashTotal,
            displayCurrency = data.displayCurrency,
            accounts        = cash,
            onEdit          = onEditAccount,
        )
        AccountGroup(
            title           = "Investments",
            total           = investTotal,
            displayCurrency = data.displayCurrency,
            accounts        = invest,
            onEdit          = onEditAccount,
        )
        AccountGroup(
            title           = "Credit",
            total           = creditTotal,
            displayCurrency = data.displayCurrency,
            accounts        = credit,
            onEdit          = onEditAccount,
        )
    }
}

// ── AccountGroup ──────────────────────────────────────────────────────────
// "CASH  $50,458" header row + surface card with AccountRows.
@Composable
private fun AccountGroup(
    title: String,
    total: Double,
    displayCurrency: String,
    accounts: List<Account>,
    onEdit: (Account) -> Unit,
) {
    if (accounts.isEmpty()) return

    Column(modifier = Modifier.padding(top = 22.dp)) {
        // group header: label left, total right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Caps(text = title, modifier = Modifier.weight(1f))
            MoneyText(
                amount = total,
                currencyCode = displayCurrency,
                size   = 14.sp,
                color  = ColorTextSecondary,
            )
        }

        // surface card with rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(ColorSurface)
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            accounts.forEachIndexed { i, account ->
                AccountRow(
                    account = account,
                    isLast   = i == accounts.lastIndex,
                    onEdit   = { onEdit(account) },
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EB, showSystemUi = true)
@Composable
private fun AccountsScreenPreview() {
    StillwaterTheme {
        AccountsScreen(data = SampleData)
    }
}
