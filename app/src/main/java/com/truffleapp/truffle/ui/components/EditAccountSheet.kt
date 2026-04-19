package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.data.UNASSIGNED_ACCOUNT_LABEL
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily

private val PillShape = RoundedCornerShape(999.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountSheet(
    account: Account,
    linkedTransactionCount: Int,
    linkedBillCount: Int,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name         by remember(account.id) { mutableStateOf(account.name) }
    var institution  by remember(account.id) { mutableStateOf(account.institution) }
    var selectedKind by remember(account.id) { mutableStateOf(account.kind) }
    var balanceText  by remember(account.id) { mutableStateOf(balanceToField(account.balance)) }
    var showDeleteConfirm by remember(account.id) { mutableStateOf(false) }

    val canSubmit = name.isNotBlank()

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
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Caps(text = "Edit account", modifier = Modifier.padding(bottom = 20.dp))

            FormField(label = "Name") {
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Account name",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
            }

            Spacer(Modifier.height(4.dp))

            FormField(label = "Institution (optional)") {
                FormTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    placeholder = "Bank or broker",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))

            Caps(text = "Type", modifier = Modifier.padding(bottom = 10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(ColorSurface)
                    .padding(2.dp),
            ) {
                AccountKind.entries.forEach { kind ->
                    val isActive = selectedKind == kind
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isActive) ColorFeature2 else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                            ) { selectedKind = kind }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = kind.name.uppercase(),
                            style = TextStyle(
                                fontFamily    = SansFamily,
                                fontSize      = 11.sp,
                                letterSpacing = 0.12.sp,
                                color         = if (isActive) ColorTextPrimary else ColorTextTertiary,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FormField(label = "Balance") {
                FormTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' || it == '-' }
                        if (filtered.count { it == '.' } <= 1) balanceText = filtered
                    },
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    onSave(
                        account.copy(
                            name        = name.trim(),
                            institution = institution.trim(),
                            balance     = balance,
                            kind        = selectedKind,
                        ),
                    )
                },
                enabled  = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = ColorInk,
                    contentColor           = ColorPage,
                    disabledContainerColor = ColorSurface,
                    disabledContentColor   = ColorTextTertiary,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
            ) {
                Caps(text = "Save", color = if (canSubmit) ColorPage else ColorTextTertiary)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Delete account",
                style = TextStyle(
                    fontFamily = SansFamily,
                    fontSize   = 13.sp,
                    color      = ColorTextTertiary,
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
        val txLine = when (linkedTransactionCount) {
            0    -> "No transactions reference this account."
            1    -> "1 transaction will be removed from Flow and Today."
            else -> "$linkedTransactionCount transactions will be removed from Flow and Today."
        }
        val billLine = when (linkedBillCount) {
            0    -> "No bills are charged to this account."
            1    -> "1 bill will show as $UNASSIGNED_ACCOUNT_LABEL until you edit it."
            else -> "$linkedBillCount bills will show as $UNASSIGNED_ACCOUNT_LABEL until you edit them."
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete “${account.name}”?",
                    style = TextStyle(fontFamily = SerifFamily, fontSize = 18.sp, color = ColorInk),
                )
            },
            text = {
                Text(
                    text = "$txLine $billLine This cannot be undone.",
                    style = TextStyle(
                        fontFamily      = SerifFamily,
                        fontStyle       = FontStyle.Italic,
                        fontSize        = 14.sp,
                        color           = ColorTextSerifBody,
                        lineHeight      = (14 * 1.55).sp,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                ) {
                    Text("Delete", style = TextStyle(fontFamily = SansFamily, color = ColorInk))
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

private fun balanceToField(balance: Double): String =
    if (balance == 0.0) ""
    else {
        val s = balance.toString()
        if (s.endsWith(".0")) s.dropLast(2) else s
    }
