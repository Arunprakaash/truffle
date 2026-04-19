package com.truffleapp.truffle.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.ui.components.Caps
import com.truffleapp.truffle.ui.components.Hairline
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import java.util.UUID

private enum class OnboardingStep { Name, Account }
private val PillShape = RoundedCornerShape(999.dp)

@Composable
fun OnboardingScreen(onComplete: (name: String, account: Account) -> Unit) {
    var step by remember { mutableStateOf(OnboardingStep.Name) }
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPage),
    ) {
        Crossfade(
            targetState = step,
            animationSpec = tween(durationMillis = 300),
            label = "onboardingStep",
        ) { currentStep ->
            when (currentStep) {
                OnboardingStep.Name -> NameStep(
                    onContinue = { enteredName ->
                        name = enteredName
                        step = OnboardingStep.Account
                    },
                )
                OnboardingStep.Account -> AccountStep(
                    userName   = name,
                    onBack     = { step = OnboardingStep.Name },
                    onComplete = { account -> onComplete(name, account) },
                )
            }
        }
    }
}

// ── Screen 1: Name ────────────────────────────────────────────────────────────

@Composable
private fun NameStep(onContinue: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val canContinue = name.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp)
            .padding(top = 0.dp, bottom = 32.dp),
    ) {
        Spacer(Modifier.weight(0.28f))

        // App intro
        Text(
            text = "A quiet place\nfor your money.",
            style = TextStyle(
                fontFamily = SerifFamily,
                fontStyle  = FontStyle.Italic,
                fontSize   = 28.sp,
                color      = ColorTextSecondary,
                lineHeight = (28 * 1.35).sp,
            ),
        )

        Spacer(Modifier.height(40.dp))

        // Question
        Caps(text = "What should we call you?", modifier = Modifier.padding(bottom = 14.dp))

        // Name input
        BasicTextField(
            value       = name,
            onValueChange = { name = it },
            modifier    = Modifier.fillMaxWidth(),
            textStyle   = TextStyle(
                fontFamily = SerifFamily,
                fontSize   = 32.sp,
                color      = ColorInk,
            ),
            singleLine  = true,
            cursorBrush = SolidColor(ColorInk),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (canContinue) onContinue(name.trim()) },
            ),
            decorationBox = { innerTextField ->
                Column {
                    Box(modifier = Modifier.padding(bottom = 10.dp)) {
                        if (name.isEmpty()) {
                            Text(
                                text  = "Your name",
                                style = TextStyle(
                                    fontFamily = SerifFamily,
                                    fontStyle  = FontStyle.Italic,
                                    fontSize   = 32.sp,
                                    color      = ColorTextTertiary,
                                ),
                            )
                        }
                        innerTextField()
                    }
                    Hairline()
                }
            },
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = { if (canContinue) onContinue(name.trim()) },
            enabled  = canContinue,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = ColorInk,
                contentColor           = ColorPage,
                disabledContainerColor = ColorSurface,
                disabledContentColor   = ColorTextTertiary,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        ) {
            Caps(text = "Continue", color = if (canContinue) ColorPage else ColorTextTertiary)
        }
    }
}

// ── Screen 2: First account ───────────────────────────────────────────────────

@Composable
private fun AccountStep(
    userName: String,
    onBack: () -> Unit,
    onComplete: (Account) -> Unit,
) {
    var accountName by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf(AccountKind.Cash) }
    val canSubmit = accountName.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp)
            .padding(top = 20.dp, bottom = 32.dp),
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ColorSurface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                modifier           = Modifier.size(16.dp),
                tint               = ColorInk,
            )
        }

        Spacer(Modifier.weight(0.2f))

        // Greet by name
        Text(
            text  = "$userName.",
            style = TextStyle(
                fontFamily = SerifFamily,
                fontSize   = 30.sp,
                color      = ColorInk,
                lineHeight = (30 * 1.2).sp,
            ),
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text  = "Where do you keep\nyour money?",
            style = TextStyle(
                fontFamily = SerifFamily,
                fontStyle  = FontStyle.Italic,
                fontSize   = 22.sp,
                color      = ColorTextSecondary,
                lineHeight = (22 * 1.4).sp,
            ),
        )

        Spacer(Modifier.height(36.dp))

        // Account name
        Caps(text = "Account name", modifier = Modifier.padding(bottom = 14.dp))

        BasicTextField(
            value         = accountName,
            onValueChange = { accountName = it },
            modifier      = Modifier.fillMaxWidth(),
            textStyle     = TextStyle(
                fontFamily = SerifFamily,
                fontSize   = 22.sp,
                color      = ColorInk,
            ),
            singleLine    = true,
            cursorBrush   = SolidColor(ColorInk),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (canSubmit) onComplete(buildAccount(accountName, selectedKind))
                },
            ),
            decorationBox = { innerTextField ->
                Column {
                    Box(modifier = Modifier.padding(bottom = 10.dp)) {
                        if (accountName.isEmpty()) {
                            Text(
                                text  = "Checking, savings…",
                                style = TextStyle(
                                    fontFamily = SerifFamily,
                                    fontStyle  = FontStyle.Italic,
                                    fontSize   = 22.sp,
                                    color      = ColorTextTertiary,
                                ),
                            )
                        }
                        innerTextField()
                    }
                    Hairline()
                }
            },
        )

        Spacer(Modifier.height(28.dp))

        // Account type selector
        Caps(text = "Type", modifier = Modifier.padding(bottom = 12.dp))

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

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = { if (canSubmit) onComplete(buildAccount(accountName, selectedKind)) },
            enabled  = canSubmit,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = ColorInk,
                contentColor           = ColorPage,
                disabledContainerColor = ColorSurface,
                disabledContentColor   = ColorTextTertiary,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        ) {
            Caps(text = "Get started", color = if (canSubmit) ColorPage else ColorTextTertiary)
        }
    }
}

private fun buildAccount(name: String, kind: AccountKind) = Account(
    id          = UUID.randomUUID().toString(),
    name        = name.trim(),
    institution = "",
    balance     = 0.0,
    kind        = kind,
)
