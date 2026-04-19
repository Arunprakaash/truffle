package com.truffleapp.truffle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.data.LedgerData
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.ui.components.BottomNavContentPadding
import com.truffleapp.truffle.ui.components.MoneyText
import com.truffleapp.truffle.ui.components.ProgressBar
import com.truffleapp.truffle.ui.components.RingProgress
import com.truffleapp.truffle.ui.components.fmt
import com.truffleapp.truffle.ui.theme.ColorFeature
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme

private val CardShape = RoundedCornerShape(14.dp)
private val PillShape = RoundedCornerShape(999.dp)

@Composable
fun GoalsScreen(
    data: LedgerData,
    modifier: Modifier = Modifier,
    onAddToGoal: (Goal) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = BottomNavContentPadding),
    ) {
        // ── Intro + list ──────────────────────────────────────────────────
        if (data.goals.isEmpty()) {
            Text(
                text = "Nothing saved toward yet.",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 16.sp,
                    color      = ColorTextSerifBody,
                    lineHeight = (16 * 1.55).sp,
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 8.dp, bottom = 14.dp),
            )
            GoalsEmptyHintCard()
        } else {
            Text(
                text = "Quiet things you are saving toward.",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 16.sp,
                    color      = ColorTextSerifBody,
                    lineHeight = (16 * 1.55).sp,
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 8.dp, bottom = 22.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                data.goals.forEach { goal ->
                    GoalCard(goal = goal, onAdd = { onAddToGoal(goal) })
                }
            }
        }
    }
}

@Composable
private fun GoalsEmptyHintCard() {
    Text(
        text = "Goals you create will live here. When you are ready, open the + menu from Today and choose Goal.",
        style = TextStyle(
            fontFamily = SansFamily,
            fontSize   = 13.sp,
            color      = ColorTextSecondary,
            lineHeight = (13 * 1.55).sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(ColorSurface)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    )
}

// ── GoalCard ──────────────────────────────────────────────────────────────
// Reference spec (screens-detail.jsx):
//   background   ColorFeature if complete, ColorSurface otherwise
//   padding      18dp vertical / 20dp horizontal
//   top row      RingProgress 54dp 1.5stroke  +  title 20sp  +  note italic 13sp tertiary
//   middle row   saved $22sp  "of $target" 13sp tertiary  |  Add pill button (ink bg)
//   bottom       ProgressBar 2dp
@Composable
private fun GoalCard(goal: Goal, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (goal.isComplete) ColorFeature else ColorSurface)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        // ── Ring + title + note ───────────────────────────────────────────
        Row(
            modifier            = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            RingProgress(value = goal.progress, size = 54.dp, strokeWidth = 1.5.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = goal.title,
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontSize   = 20.sp,
                        color      = ColorInk,
                        lineHeight = (20 * 1.2).sp,
                    ),
                )
                Text(
                    text     = goal.note,
                    style    = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 13.sp,
                        color      = ColorTextSerifMuted,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // ── Saved amount + Add button ──────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom,
        ) {
            // "$38,200 of $80,000"
            Row(verticalAlignment = Alignment.Bottom) {
                MoneyText(amount = goal.saved, size = 22.sp)
                Text(
                    text  = buildAnnotatedString {
                        withStyle(SpanStyle(fontFamily = SerifFamily)) {
                            append(" of ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily          = SerifFamily,
                                fontFeatureSettings = "\"tnum\" on",
                            )
                        ) {
                            append(fmt(goal.target))
                        }
                    },
                    style = TextStyle(fontSize = 13.sp, color = ColorTextSerifMuted),
                )
            }

            // Add / Complete button
            AddButton(isComplete = goal.isComplete, onClick = onAdd)
        }

        // ── Progress bar ──────────────────────────────────────────────────
        ProgressBar(value = goal.progress)
    }
}

// ── Add button ─────────────────────────────────────────────────────────────
// Ink pill when active, transparent + tertiary text when goal is complete.
@Composable
private fun AddButton(isComplete: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (isComplete) ColorSurface else ColorInk)
            .clickable(
                enabled           = !isComplete,
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isComplete) {
            Text(
                text  = "Complete",
                style = TextStyle(
                    fontFamily = SansFamily,
                    fontSize   = 11.sp,
                    color      = ColorTextTertiary,
                ),
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Add",
                    style = TextStyle(
                        fontFamily = SansFamily,
                        fontSize   = 11.sp,
                        color      = ColorPage,
                    ),
                )
                Icon(
                    imageVector      = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier         = Modifier.size(11.dp),
                    tint             = ColorPage,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EB, showSystemUi = true)
@Composable
private fun GoalsScreenPreview() {
    StillwaterTheme {
        GoalsScreen(data = SampleData)
    }
}
