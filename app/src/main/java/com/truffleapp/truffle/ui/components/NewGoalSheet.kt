package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGoalSheet(
    onDismiss: () -> Unit,
    onAdd: (Goal) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title      by remember { mutableStateOf("") }
    var note       by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    val canSubmit = title.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0

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

            Caps(text = "New goal", modifier = Modifier.padding(bottom = 20.dp))

            FormField(label = "What are you saving for?") {
                FormTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "A trip, a camera, a cushion…",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            }

            Spacer(Modifier.height(4.dp))

            FormField(label = "A quiet note") {
                FormTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "Why this matters to you…",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            }

            Spacer(Modifier.height(4.dp))

            FormField(label = "Target amount") {
                FormTextField(
                    value = targetText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) targetText = filtered
                    },
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: return@Button
                    onAdd(
                        Goal(
                            id     = UUID.randomUUID().toString(),
                            title  = title.trim(),
                            note   = note.trim().ifEmpty { "Something worth working toward." },
                            saved  = 0.0,
                            target = target,
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
                Caps(text = "Begin saving", color = if (canSubmit) ColorPage else ColorTextTertiary)
            }
        }
    }
}
