package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.BillRecurrence
import com.truffleapp.truffle.data.rowHint
import com.truffleapp.truffle.data.DEFAULT_LEDGER_CURRENCY
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme

// ── BillRow ───────────────────────────────────────────────────────────────
// Reference spec (screens-home.jsx):
//   padding        13px 14px
//   gap            12dp
//   left cell      34×34 box with days-until-due number (serif 15sp, ColorTextSerifMuted)
//   label          sans 13sp weight 500 ink
//   sub            serif italic 11.5sp muted — "in X days · Account"
//   amount         serif 15sp ColorTextSerifBody, shown negative (outflow)
//   hairline       starts at 60dp

@Composable
fun BillRow(
    bill: Bill,
    modifier: Modifier = Modifier,
    currencyCode: String = DEFAULT_LEDGER_CURRENCY,
    isLast: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Days-till-due — same footprint as IconCircle, but just a number
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bill.daysUntilDue().coerceAtLeast(0).toString(),
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontSize = 15.sp,
                        color = ColorTextSerifMuted,
                        fontFeatureSettings = "\"tnum\" on",
                    ),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Label + sub-label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.label,
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ColorInk,
                    ),
                )
                Text(
                    text = buildString {
                        append(bill.relativeDuePhrase())
                        append(" · ")
                        append(bill.account)
                        if (bill.recurrence != BillRecurrence.None) {
                            append(" · ")
                            append(bill.recurrence.rowHint())
                        }
                    },
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

            // Amount — always an outflow so passed as negative
            MoneyText(
                amount = -bill.amount,
                currencyCode = currencyCode,
                size = 15.sp,
                cents = true,
                color = ColorTextSerifBody,
            )
        }

        if (!isLast) {
            Hairline(modifier = Modifier.padding(start = 60.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEDE8DF)
@Composable
private fun BillRowPreview() {
    StillwaterTheme {
        Column {
            SampleData.bills.filter { !it.paid }.take(3).forEachIndexed { i, bill ->
                BillRow(bill = bill, isLast = i == 2)
            }
        }
    }
}
