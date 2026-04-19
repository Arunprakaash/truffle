package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SerifFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTypeSheet(
    onTransaction: () -> Unit,
    onBill: () -> Unit,
    onGoal: () -> Unit,
    onAccount: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

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
                .padding(top = 20.dp, bottom = 16.dp)
                .navigationBarsPadding(),
        ) {
            // drag handle
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Caps(text = "What are you adding?", modifier = Modifier.padding(bottom = 16.dp))

            TypeOption(
                icon = Icons.Outlined.ReceiptLong,
                title = "Transaction",
                note = "Record spending or income",
                onClick = onTransaction,
            )
            Hairline()
            TypeOption(
                icon = Icons.Outlined.Flag,
                title = "Bill",
                note = "Something upcoming and expected",
                onClick = onBill,
            )
            Hairline()
            TypeOption(
                icon = Icons.Outlined.Savings,
                title = "Goal",
                note = "Something you are quietly working toward",
                onClick = onGoal,
            )
            Hairline()
            TypeOption(
                icon = Icons.Outlined.AccountBalance,
                title = "Account",
                note = "Another place your money lives",
                onClick = onAccount,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TypeOption(
    icon: ImageVector,
    title: String,
    note: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(
            imageVector = icon,
            size = 38.dp,
            iconSize = 16.dp,
            background = ColorFeature2,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 18.sp,
                    color = ColorInk,
                ),
            )
            Text(
                text = note,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = ColorTextTertiary,
                ),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
