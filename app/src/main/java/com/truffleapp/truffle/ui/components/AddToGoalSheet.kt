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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SerifFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToGoalSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (goalId: String, amount: Double) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Slider range: 10–1000, step 10 → use float state, snap to nearest 10
    var sliderValue by remember { mutableFloatStateOf(100f) }
    val amount = (sliderValue / 10).toInt() * 10  // snap to nearest 10

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

            // Amount display — centered, large tabular serif
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$$amount",
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontSize = 54.sp,
                        color = ColorInk,
                        lineHeight = 54.sp,
                        fontFeatureSettings = "\"tnum\" on",
                    ),
                )
                Text(
                    text = "from Checking",
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = ColorTextSerifMuted,
                    ),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            // Slider
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 10f..1000f,
                steps = 98,  // (1000-10)/10 - 1 = 98 steps for 10-unit increments
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = SliderDefaults.colors(
                    thumbColor = ColorInk,
                    activeTrackColor = ColorInk,
                    inactiveTrackColor = ColorSurface,
                ),
            )

            // Quick-add pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf(50, 100, 250, 500).forEach { preset ->
                    val isActive = amount == preset
                    Text(
                        text = "$$preset",
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontSize = 12.sp,
                            color = ColorInk,
                            fontFeatureSettings = "\"tnum\" on",
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isActive) ColorFeature2 else ColorSurface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { sliderValue = preset.toFloat() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }

            // Transfer button
            Button(
                onClick = { onConfirm(goal.id, amount.toDouble()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorInk,
                    contentColor = ColorPage,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
            ) {
                Caps(text = "Transfer quietly", color = ColorPage)
            }
        }
    }
}
