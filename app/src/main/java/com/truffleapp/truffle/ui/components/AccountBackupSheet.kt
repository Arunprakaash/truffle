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
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.UploadFile
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
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SerifFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBackupSheet(
    onDismiss: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onRequestClearAll: () -> Unit,
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
                .padding(top = 20.dp, bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Caps(text = "Data & backup", modifier = Modifier.padding(bottom = 6.dp))

            Text(
                text = "Import, export, or wipe this device’s ledger.",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 12.sp,
                    color      = ColorTextSerifMuted,
                ),
                modifier = Modifier.padding(bottom = 14.dp),
            )

            BackupActionRow(
                icon  = Icons.Outlined.UploadFile,
                title = "Import backup",
                note  = "Restore from a Truffle JSON file",
                onClick = {
                    onImport()
                    onDismiss()
                },
            )
            Hairline()
            BackupActionRow(
                icon  = Icons.Outlined.FileDownload,
                title = "Export backup",
                note  = "Share a JSON copy of your ledger",
                onClick = {
                    onExport()
                    onDismiss()
                },
            )
            Hairline()
            BackupActionRow(
                icon  = Icons.Outlined.DeleteForever,
                title = "Clear all data",
                note  = "Remove everything and set up again",
                onClick = {
                    onRequestClearAll()
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun BackupActionRow(
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
                indication        = null,
                onClick             = onClick,
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(
            imageVector = icon,
            size        = 38.dp,
            iconSize    = 16.dp,
            background  = ColorFeature2,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize   = 18.sp,
                    color      = ColorInk,
                ),
            )
            Text(
                text = note,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 12.sp,
                    color      = ColorTextSerifMuted,
                ),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
