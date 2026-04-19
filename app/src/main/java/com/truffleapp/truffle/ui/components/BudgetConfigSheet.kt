package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.truffleapp.truffle.data.primaryAmountCurrency
import com.truffleapp.truffle.data.LedgerData
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetConfigSheet(
    data: LedgerData,
    onDismiss: () -> Unit,
    onSave: (Map<String, Double>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val budgets = data.budgets
    val limitTextById = remember(data.budgets) {
        mutableStateMapOf<String, String>().apply {
            data.budgets.forEach { b ->
                val t = b.limit.toString()
                put(b.id, if (t.endsWith(".0")) t.dropLast(2) else t)
            }
        }
    }
    val dc = data.primaryAmountCurrency()

    val canSave = budgets.all { b ->
        (limitTextById[b.id]?.toDoubleOrNull() ?: -1.0) >= 0.0
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
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(20.dp))
            Caps(text = "Budget limits", modifier = Modifier.padding(bottom = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                budgets.forEach { b ->
                    FormField(label = "${b.label} · monthly cap ($dc)") {
                        FormTextField(
                            value = limitTextById[b.id] ?: "",
                            onValueChange = { raw ->
                                limitTextById[b.id] = raw.filter { ch -> ch.isDigit() || ch == '.' }
                            },
                            placeholder = "0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val parsed = budgets.associate { b ->
                        b.id to (limitTextById[b.id]?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: b.limit)
                    }
                    onSave(parsed)
                },
                enabled = canSave,
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
                Caps(
                    text = "Save",
                    color = if (canSave) ColorPage else ColorTextTertiary,
                )
            }
        }
    }
}
