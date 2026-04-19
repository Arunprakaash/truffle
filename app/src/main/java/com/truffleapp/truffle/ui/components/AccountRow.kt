package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme
import kotlin.math.abs

// ── AccountRow ────────────────────────────────────────────────────────────
// Reference spec (screens-home.jsx):
//   Same structure as TxRow — icon circle 34dp, gap 12dp, padding 13px 14px
//   name           sans 13sp weight 500 ink
//   institution    serif italic 11.5sp tertiary
//   balance        serif 15sp — cents shown when |balance| < $100k
//   hairline       starts at 60dp

@Composable
fun AccountRow(
    account: Account,
    modifier: Modifier = Modifier,
    isLast: Boolean = false,
    onEdit: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClickLabel = "Edit ${account.name}",
                    onClick = onEdit,
                    onLongClickLabel = "Edit account",
                    onLongClick = onEdit,
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCircle(
                imageVector = accountIcon(account.kind),
                size = 34.dp,
                iconSize = 14.dp,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ColorInk,
                    ),
                )
                Text(
                    text = account.institution,
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 11.5.sp,
                        color = ColorTextSerifMuted,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Show cents when balance is under $100k — matches reference logic
            MoneyText(
                amount = account.balance,
                currencyCode = account.currency,
                size = 15.sp,
                cents = abs(account.balance) < 100_000,
            )
        }

        if (!isLast) {
            Hairline(modifier = Modifier.padding(start = 60.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEDE8DF)
@Composable
private fun AccountRowPreview() {
    StillwaterTheme {
        Column {
            SampleData.accounts.forEachIndexed { i, account ->
                AccountRow(account = account, isLast = i == SampleData.accounts.lastIndex)
            }
        }
    }
}
