package com.truffleapp.truffle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.CATEGORIES
import com.truffleapp.truffle.data.RECATEGORIZABLE
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxDetailSheet(
    tx: Transaction,
    onDismiss: () -> Unit,
    onRecategorize: (txId: String, category: String) -> Unit,
    onRemove: (txId: String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentCategory by remember { mutableStateOf(tx.category) }
    var showCats by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val cat = CATEGORIES[currentCategory] ?: CATEGORIES[tx.category]

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
                .verticalScroll(rememberScrollState())
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
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            // Header row: date/merchant + close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Caps(text = "${tx.date}, ${tx.time}", modifier = Modifier.padding(bottom = 6.dp))
                    Text(
                        text = tx.merchant,
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontSize = 22.sp,
                            color = ColorInk,
                        ),
                    )
                }
                // close button
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ColorSurface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(14.dp),
                        tint = ColorInk,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Amount
            MoneyText(amount = tx.amount, size = 40.sp, sign = true)

            Spacer(Modifier.height(10.dp))

            // Note
            Text(
                text = "${tx.note}.",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = ColorTextSecondary,
                    lineHeight = (14 * 1.55).sp,
                ),
            )

            Hairline(modifier = Modifier.padding(top = 22.dp, bottom = 4.dp))

            // Detail rows
            DetailRow(label = "Paid from", value = tx.account)
            DetailRow(
                label = "Category",
                value = cat?.label ?: tx.category,
                icon = cat?.icon,
                onClick = { showCats = !showCats },
                chevron = true,
            )

            // Category picker
            AnimatedVisibility(
                visible = showCats,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSurface)
                        .padding(6.dp),
                ) {
                    RECATEGORIZABLE.entries.forEach { (key, info) ->
                        val isSelected = currentCategory == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ColorFeature2 else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    currentCategory = key
                                    onRecategorize(tx.id, key)
                                    showCats = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconCircle(
                                imageVector = categoryIcon(info.icon),
                                size = 28.dp,
                                iconSize = 12.dp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = info.label,
                                style = TextStyle(
                                    fontFamily = SansFamily,
                                    fontSize = 13.sp,
                                    color = ColorInk,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = ColorInk,
                                )
                            }
                        }
                    }
                }
            }

            DetailRow(label = "Status", value = "Cleared")

            Hairline(modifier = Modifier.padding(top = 12.dp, bottom = 20.dp))

            // Reflection note
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorSurface)
                    .padding(16.dp),
            ) {
                Caps(text = "A note", modifier = Modifier.padding(bottom = 8.dp))
                val noteText = if (tx.amount < 0) {
                    "You have spent ${fmt(abs(tx.amount), cents = true)} at ${tx.merchant} this month. Still within what you intended."
                } else {
                    "Received. The kind of arrival that asks nothing in return."
                }
                Text(
                    text = noteText,
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = ColorTextSecondary,
                        lineHeight = (14 * 1.6).sp,
                    ),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Remove from ledger",
                style = TextStyle(
                    fontFamily = SansFamily,
                    fontSize   = 13.sp,
                    color        = ColorTextTertiary,
                ),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                    ) { showDeleteConfirm = true },
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Remove this transaction?",
                    style = TextStyle(fontFamily = SerifFamily, fontSize = 18.sp, color = ColorInk),
                )
            },
            text = {
                Text(
                    text = "It will disappear from Flow and Today, and the linked account balance will move back as if it never happened.",
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
                        showDeleteConfirm = false
                        onRemove(tx.id)
                        onDismiss()
                    },
                ) {
                    Text("Remove", style = TextStyle(fontFamily = SansFamily, color = ColorInk))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", style = TextStyle(fontFamily = SansFamily, color = ColorTextTertiary))
                }
            },
        )
    }
}

// ── DetailRow ─────────────────────────────────────────────────────────────────
@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: String? = null,
    onClick: (() -> Unit)? = null,
    chevron: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Caps(text = label, modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconCircle(
                    imageVector = categoryIcon(icon),
                    size = 22.dp,
                    iconSize = 10.dp,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 15.sp,
                    color = ColorInk,
                ),
            )
            if (chevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(10.dp),
                    tint = ColorTextTertiary,
                )
            }
        }
    }
    Hairline()
}
