package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme

// ── TxRow ─────────────────────────────────────────────────────────────────
// Reference spec (screens-home.jsx):
//   padding        11px 14px
//   gap            12dp between icon and text columns
//   icon circle    34dp, iconSize 14dp
//   merchant       sans 13sp weight 500 ink, marginBottom 2dp
//   note           serif italic 11.5sp tertiary, ellipsis
//   amount         serif 15sp ink, tabular nums, sign prefix
//   time           10sp tertiary, marginTop 2dp
//   hairline       starts at 60dp (14 padding + 34 icon + 12 gap)

@Composable
fun TxRow(
    tx: Transaction,
    modifier: Modifier = Modifier,
    isLast: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category icon circle
            IconCircle(
                imageVector = categoryIcon(tx.icon),
                size = 34.dp,
                iconSize = 14.dp,
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Merchant + note
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.merchant,
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ColorInk,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = tx.note,
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 11.5.sp,
                        color = ColorTextSerifMuted,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount + time
            Column(horizontalAlignment = Alignment.End) {
                MoneyText(
                    amount = tx.amount,
                    size = 15.sp,
                    cents = true,
                    sign = true,
                    textAlign = TextAlign.End,
                )
                Text(
                    text = tx.time,
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontSize = 10.sp,
                        color = ColorTextTertiary,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        if (!isLast) {
            // Hairline indented to sit under text, not under icon
            Hairline(modifier = Modifier.padding(start = 60.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEDE8DF)
@Composable
private fun TxRowPreview() {
    StillwaterTheme {
        Column {
            TxRow(tx = SampleData.transactions[0], isLast = false)
            TxRow(tx = SampleData.transactions[1], isLast = false)
            TxRow(tx = SampleData.transactions[2], isLast = true)
        }
    }
}
