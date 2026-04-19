package com.truffleapp.truffle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.LEDGER_CURRENCY_OPTIONS
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily

private val PickerSurfaceShape = RoundedCornerShape(12.dp)
private val PickerRowShape = RoundedCornerShape(8.dp)

/** Same interaction as transaction category: in-column expand + scroll (no [DropdownMenu]). */
@Composable
fun CurrencySelector(
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Currency",
) {
    val normalized = normalizeLedgerCurrencyCode(selectedCode)
    var showPicker by remember { mutableStateOf(false) }
    val labelFor = remember(normalized) {
        LEDGER_CURRENCY_OPTIONS.find { it.code == normalized }?.label ?: normalized
    }

    Column(modifier = modifier) {
        Caps(text = label, modifier = Modifier.padding(bottom = 10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showPicker = !showPicker }
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$normalized · $labelFor",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 16.sp,
                    color = ColorInk,
                ),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = ColorTextTertiary,
            )
        }
        Hairline()

        AnimatedVisibility(
            visible = showPicker,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .padding(top = 8.dp)
                    .clip(PickerSurfaceShape)
                    .background(ColorSurface)
                    .padding(6.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                LEDGER_CURRENCY_OPTIONS.forEach { opt ->
                    val isSelected = opt.code == normalized
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PickerRowShape)
                            .background(if (isSelected) ColorFeature2 else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                onSelect(opt.code)
                                showPicker = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${opt.code} · ${opt.label}",
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

        Hairline(modifier = Modifier.padding(top = 4.dp))
    }
}
