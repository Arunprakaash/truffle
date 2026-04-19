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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import java.util.UUID

private val PillShape = RoundedCornerShape(999.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountSheet(
    onDismiss: () -> Unit,
    onAdd: (Account) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name         by remember { mutableStateOf("") }
    var institution  by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf(AccountKind.Cash) }
    var balanceText  by remember { mutableStateOf("") }

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

            Caps(text = "New account", modifier = Modifier.padding(bottom = 20.dp))

            FormField(label = "Name") {
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Checking, savings…",
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

            FormField(label = "Starting balance") {
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
                    onAdd(
                        Account(
                            id           = UUID.randomUUID().toString(),
                            name         = name.trim(),
                            institution  = institution.trim(),
                            balance      = balance,
                            kind         = selectedKind,
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
                Caps(text = "Add account", color = if (canSubmit) ColorPage else ColorTextTertiary)
            }
        }
    }
}
