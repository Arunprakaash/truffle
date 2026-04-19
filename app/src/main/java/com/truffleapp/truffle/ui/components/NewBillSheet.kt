package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SerifFamily
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private val BillDueDateDisplayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")

private fun LocalDate.toPickerUtcMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun millisToLocalEpochDay(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBillSheet(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onAdd: (Bill) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var label         by remember { mutableStateOf("") }
    var amountText    by remember { mutableStateOf("") }
    var dueDateEpoch  by remember { mutableLongStateOf(LocalDate.now().plusDays(7).toEpochDay()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var accountIdx    by remember { mutableIntStateOf(0) }

    val canSubmit = label.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0

    if (showDatePicker) {
        val initialMillis = LocalDate.ofEpochDay(dueDateEpoch).toPickerUtcMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: initialMillis
                        dueDateEpoch = millisToLocalEpochDay(millis)
                        showDatePicker = false
                    },
                ) { Text("Save", color = ColorInk) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = ColorTextTertiary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
            // Drag handle
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Caps(text = "New bill", modifier = Modifier.padding(bottom = 20.dp))

            // Label
            FormField(label = "Bill name") {
                FormTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = "Rent, Netflix, electricity…",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
            }

            Spacer(Modifier.height(4.dp))

            // Amount
            FormField(label = "Amount") {
                FormTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) amountText = filtered
                    },
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }

            Spacer(Modifier.height(4.dp))

            FormField(label = "Due date") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { showDatePicker = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = LocalDate.ofEpochDay(dueDateEpoch).format(BillDueDateDisplayFormatter),
                            style = TextStyle(
                                fontFamily = SerifFamily,
                                fontSize = 16.sp,
                                color = ColorInk,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "tap to change",
                            style = TextStyle(
                                fontFamily = SerifFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.sp,
                                color = ColorTextTertiary,
                            ),
                        )
                    }
                    Hairline()
                    val today = LocalDate.now()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        BillDuePresetLink("Today") { dueDateEpoch = today.toEpochDay() }
                        BillDuePresetLink("+7 days") { dueDateEpoch = today.plusDays(7).toEpochDay() }
                        BillDuePresetLink("+30 days") { dueDateEpoch = today.plusDays(30).toEpochDay() }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Account
            if (accounts.isNotEmpty()) {
                val account = accounts[accountIdx % accounts.size]
                Column(modifier = Modifier.fillMaxWidth()) {
                    Caps(text = "From account", modifier = Modifier.padding(bottom = 10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { accountIdx = (accountIdx + 1) % accounts.size }
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = account.name,
                            style = TextStyle(
                                fontFamily = SerifFamily,
                                fontSize = 16.sp,
                                color = ColorInk,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "tap to change",
                            style = TextStyle(
                                fontFamily = SerifFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.sp,
                                color = ColorTextTertiary,
                            ),
                        )
                    }
                    Hairline()
                }

                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    onAdd(
                        Bill(
                            id            = UUID.randomUUID().toString(),
                            label         = label.trim(),
                            amount        = amount,
                            dueDateEpoch  = dueDateEpoch,
                            paid          = false,
                            account       = accounts.getOrNull(accountIdx % accounts.size)?.name ?: "",
                        )
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorInk,
                    contentColor = ColorPage,
                    disabledContainerColor = ColorSurface,
                    disabledContentColor = ColorTextTertiary,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
            ) {
                Caps(text = "Add bill", color = if (canSubmit) ColorPage else ColorTextTertiary)
            }
        }
    }
}

@Composable
private fun BillDuePresetLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        style = TextStyle(
            fontFamily = SerifFamily,
            fontSize = 13.sp,
            color = ColorInk,
        ),
    )
}
