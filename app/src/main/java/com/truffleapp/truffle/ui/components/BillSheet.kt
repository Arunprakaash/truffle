package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.BillRecurrence
import com.truffleapp.truffle.data.pickerLabel
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SerifFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSheet(
    bill: Bill,
    currencyCode: String,
    onDismiss: () -> Unit,
    onMarkPaid: (billId: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorPage,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 32.dp)
                .navigationBarsPadding(),
        ) {
            // drag handle
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(com.truffleapp.truffle.ui.theme.ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Caps(
                text = "${bill.relativeDuePhrase()} · ${bill.dueDateShortLabel()}",
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = bill.label,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorInk,
                ),
            )

            Spacer(Modifier.height(14.dp))

            MoneyText(
                amount = -bill.amount,
                currencyCode = currencyCode,
                size = 44.sp,
                cents = true,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "From ${bill.account}. Auto-drafted, as arranged.",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    color = ColorTextSerifMuted,
                    lineHeight = (17 * 1.55).sp,
                ),
            )

            if (bill.recurrence != BillRecurrence.None) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = bill.recurrence.pickerLabel(),
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = ColorTextSerifMuted,
                        lineHeight = (13 * 1.5).sp,
                    ),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorSurface,
                        contentColor = ColorInk,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                ) {
                    Caps(text = "Close")
                }

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = { onMarkPaid(bill.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorInk,
                        contentColor = ColorPage,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                ) {
                    Caps(text = "Mark paid", color = ColorPage)
                }
            }
        }
    }
}
