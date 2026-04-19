package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.LEDGER_CURRENCY_OPTIONS
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorMuted
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.SansFamily

@Composable
fun CurrencySelector(
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Currency",
) {
    val normalized = normalizeLedgerCurrencyCode(selectedCode)
    var expanded by remember { mutableStateOf(false) }
    val labelFor = remember(normalized) {
        LEDGER_CURRENCY_OPTIONS.find { it.code == normalized }?.label ?: normalized
    }

    Column(modifier = modifier) {
        Caps(text = label, modifier = Modifier.padding(bottom = 8.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                    ) { expanded = true }
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "$normalized · $labelFor",
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontSize   = 14.sp,
                        color      = ColorInk,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Change",
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontSize   = 12.sp,
                        color      = ColorMuted,
                    ),
                )
            }
            DropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                containerColor   = ColorSurface,
            ) {
                LEDGER_CURRENCY_OPTIONS.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text("${opt.code} · ${opt.label}") },
                        onClick = {
                            onSelect(opt.code)
                            expanded = false
                        },
                    )
                }
            }
        }
        Hairline(modifier = Modifier.padding(top = 4.dp))
    }
}
