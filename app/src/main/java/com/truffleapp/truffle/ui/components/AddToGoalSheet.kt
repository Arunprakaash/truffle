package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.data.formatLedgerMoney
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SerifFamily
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

private fun sliderHapticBucket(
    value: Float,
    minSlider: Float,
    maxSlider: Float,
    useTenStep: Boolean,
): Int {
    val x = value.coerceIn(minSlider, maxSlider)
    if (useTenStep) return (round(x / 10f) * 10f).toInt()
    val span = maxSlider - minSlider
    if (span <= 1e-6f) return 0
    return (((x - minSlider) / span) * 28f).toInt().coerceIn(0, 27)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToGoalSheet(
    goal: Goal,
    /** ISO code for formatting the transfer amount (usually the source account’s currency). */
    amountCurrencyCode: String,
    fromAccountLabel: String,
    /** Spendable balance available to move (cash / invest; credit is treated as 0). */
    maxFromAccount: Double,
    fromAccountId: String,
    onDismiss: () -> Unit,
    onConfirm: (goalId: String, amount: Double, fromAccountId: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = rememberHaptics()
    val dc = normalizeLedgerCurrencyCode(amountCurrencyCode)
    val maxV = maxFromAccount.coerceAtLeast(0.0)
    val useTenStep = maxV >= 10.0
    val minSlider = when {
        maxV <= 0.0 -> 0f
        useTenStep -> 10f
        else -> 1f.coerceAtMost(maxV.toFloat())
    }
    val maxSlider = when {
        maxV <= 0.0 -> 1f
        useTenStep -> min(1000.0, maxV).toFloat().coerceAtLeast(minSlider)
        else -> maxV.toFloat().coerceAtLeast(minSlider)
    }

    val initial = remember(goal.id, maxFromAccount, minSlider, maxSlider) {
        when {
            maxV <= 0.0 -> 0f
            useTenStep -> {
                val target = min(100f, max(minSlider, (maxV * 0.25).toFloat()))
                (round(target / 10f) * 10f).coerceIn(minSlider, maxSlider)
            }
            else -> ((minSlider + maxSlider) / 2f).coerceIn(minSlider, maxSlider)
        }
    }
    var sliderValue by remember(goal.id, maxFromAccount) { mutableFloatStateOf(initial) }
    val lastHapticBucket = remember(goal.id, maxFromAccount, minSlider, maxSlider, useTenStep) {
        mutableIntStateOf(
            sliderHapticBucket(
                initial.coerceIn(minSlider, maxSlider),
                minSlider,
                maxSlider,
                useTenStep,
            ),
        )
    }

    LaunchedEffect(minSlider, maxSlider, useTenStep) {
        val c = sliderValue.coerceIn(minSlider, maxSlider)
        sliderValue = c
        lastHapticBucket.intValue = sliderHapticBucket(c, minSlider, maxSlider, useTenStep)
    }

    val amount = when {
        maxV <= 0.0 -> 0.0
        useTenStep -> sliderValue.toDouble().coerceIn(minSlider.toDouble(), maxSlider.toDouble())
        else -> (round(sliderValue.toDouble() * 100.0) / 100.0).coerceIn(minSlider.toDouble(), maxSlider.toDouble())
    }

    val stepsTen = remember(minSlider, maxSlider, useTenStep) {
        if (!useTenStep) 0
        else (((maxSlider - minSlider) / 10f).toInt() - 1).coerceAtLeast(0)
    }

    val canTransfer =
        fromAccountId.isNotBlank() && maxV > 0 && amount > 0 && amount <= maxV + 1e-6

    val presets = remember(maxV, useTenStep, minSlider) {
        if (useTenStep) {
            listOf(50.0, 100.0, 250.0, 500.0).filter { it <= maxV + 1e-6 && it >= minSlider.toDouble() }
        } else {
            listOf(1.0, 2.0, 5.0).filter { it <= maxV + 1e-6 && it >= minSlider.toDouble() }
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
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Caps(text = "Add toward", modifier = Modifier.padding(bottom = 6.dp))

            Text(
                text = goal.title,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 24.sp,
                    color = ColorInk,
                ),
            )

            Text(
                text = goal.note,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = ColorTextSerifMuted,
                ),
                modifier = Modifier.padding(top = 4.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (maxV <= 0.0) {
                    Text(
                        text = "No spendable balance in $fromAccountLabel right now.",
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            color = ColorTextSerifMuted,
                        ),
                    )
                } else {
                    Text(
                        text = formatLedgerMoney(amount, dc, cents = !useTenStep),
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontSize = 54.sp,
                            color = ColorInk,
                            lineHeight = 54.sp,
                            fontFeatureSettings = "\"tnum\" on",
                        ),
                    )
                    Text(
                        text = "from $fromAccountLabel · up to ${formatLedgerMoney(maxV, dc, cents = !useTenStep)} available",
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = ColorTextSerifMuted,
                        ),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            if (maxV > 0.0) {
                Slider(
                    value = sliderValue.coerceIn(minSlider, maxSlider),
                    onValueChange = { v ->
                        val next = if (useTenStep) {
                            (round(v / 10f) * 10f).coerceIn(minSlider, maxSlider)
                        } else {
                            v.coerceIn(minSlider, maxSlider)
                        }
                        val bucket = sliderHapticBucket(next, minSlider, maxSlider, useTenStep)
                        if (bucket != lastHapticBucket.intValue) {
                            lastHapticBucket.intValue = bucket
                            haptics.tick()
                        }
                        sliderValue = next
                    },
                    valueRange = minSlider..maxSlider,
                    steps = stepsTen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = ColorInk,
                        activeTrackColor = ColorInk,
                        inactiveTrackColor = ColorSurface,
                    ),
                )

                if (presets.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        presets.forEach { preset ->
                            val presetFloat = preset.toFloat()
                            val active = if (useTenStep) {
                                kotlin.math.abs(amount - preset) < 0.5
                            } else {
                                kotlin.math.abs(amount - preset) < 0.005
                            }
                            Text(
                                text = formatLedgerMoney(preset, dc, cents = !useTenStep),
                                style = TextStyle(
                                    fontFamily = SerifFamily,
                                    fontSize = 12.sp,
                                    color = ColorInk,
                                    fontFeatureSettings = "\"tnum\" on",
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (active) ColorFeature2 else ColorSurface)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        val next = presetFloat.coerceIn(minSlider, maxSlider)
                                        if (next != sliderValue) {
                                            lastHapticBucket.intValue =
                                                sliderHapticBucket(next, minSlider, maxSlider, useTenStep)
                                            haptics.click()
                                            sliderValue = next
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onConfirm(goal.id, amount, fromAccountId) },
                enabled = canTransfer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorInk,
                    contentColor = ColorPage,
                    disabledContainerColor = ColorSurface,
                    disabledContentColor = ColorTextSerifMuted,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
            ) {
                Caps(
                    text = "Transfer quietly",
                    color = if (canTransfer) ColorPage else ColorTextSerifMuted,
                )
            }
        }
    }
}
