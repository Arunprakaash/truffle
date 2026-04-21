package com.truffleapp.truffle.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.speech.SpeechRecognizer
import android.widget.Toast
import com.truffleapp.truffle.ml.EntityNer
import com.truffleapp.truffle.ml.TransactionExtractor
import com.truffleapp.truffle.ml.recognizeSpeech
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.data.canCoverExpense
import com.truffleapp.truffle.data.CATEGORIES
import com.truffleapp.truffle.data.DEFAULT_LEDGER_CURRENCY
import com.truffleapp.truffle.data.RECATEGORIZABLE
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.data.ledgerCurrencySymbol
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val PillShape = RoundedCornerShape(999.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    accounts: List<Account>,
    displayCurrency: String = DEFAULT_LEDGER_CURRENCY,
    onDismiss: () -> Unit,
    onAdd: (Transaction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val haptics = rememberHaptics()
    val scope   = rememberCoroutineScope()

    var amountText         by remember { mutableStateOf("") }
    var isExpense          by remember { mutableStateOf(true) }
    var merchant           by remember { mutableStateOf("") }
    var category           by remember { mutableStateOf("food") }
    var note               by remember { mutableStateOf("") }
    var accountIdx         by remember { mutableIntStateOf(0) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var capturedLocation   by remember { mutableStateOf<Location?>(null) }
    var isListening        by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    var nerModel by remember { mutableStateOf<EntityNer?>(null) }
    LaunchedEffect(Unit) { try { nerModel = EntityNer() } catch (_: Exception) { } }
    DisposableEffect(Unit) { onDispose { nerModel?.close() } }

    val speechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    fun applyVoiceResult(raw: String) {
        scope.launch {
            isListening = false
            if (raw.isBlank()) { toast("Couldn't hear anything — try again"); return@launch }
            val spans  = withContext(Dispatchers.IO) { nerModel?.predict(raw) ?: emptyList() }
            val result = TransactionExtractor.extract(spans, raw)
            result.merchant?.let { merchant = it }
            result.amount?.let   { amountText = it.toBigDecimal().stripTrailingZeros().toPlainString() }
            result.note?.let     { note = it }
            haptics.click()
        }
    }

    fun startVoice() {
        if (!speechAvailable) { toast("Speech recognition not available on this device"); return }
        scope.launch {
            isListening = true
            haptics.tick()
            try {
                val text = recognizeSpeech(context)
                applyVoiceResult(text)
            } catch (e: Exception) {
                isListening = false
                toast(e.message ?: "Mic error — try again")
            }
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startVoice() else toast("Microphone permission denied") }

    val ledgerDc = normalizeLedgerCurrencyCode(displayCurrency)
    val amountRowCurrency = remember(ledgerDc, accountIdx, accounts) {
        accounts.getOrNull(accountIdx)?.let { normalizeLedgerCurrencyCode(it.currency) } ?: ledgerDc
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val selectedAccount = accounts.getOrNull(accountIdx)
    val hasFundsForExpense =
        !isExpense ||
            selectedAccount == null ||
            selectedAccount.canCoverExpense(parsedAmount)
    val canSubmit = parsedAmount > 0 && merchant.isNotBlank() && hasFundsForExpense

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            capturedLocation = getBestLastLocation(context)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 32.dp)
                .navigationBarsPadding(),
        ) {
            // Centered drag handle
            Spacer(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderTertiary)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            // Mic button right-aligned, mirroring the close button pattern in other sheets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                SurfaceCircleIconButton(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice input",
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED
                        ) startVoice()
                        else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    containerColor = if (isListening) ColorInk else ColorSurface,
                    iconTint       = if (isListening) ColorPage else ColorTextTertiary,
                    size    = 30.dp,
                    iconSize = 14.dp,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Out / In toggle ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(ColorSurface)
                    .padding(2.dp),
            ) {
                listOf(true to "Expense", false to "Income").forEach { (expense, label) ->
                    val active = isExpense == expense
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (active) ColorFeature2 else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (isExpense != expense) {
                                    haptics.tick()
                                    isExpense = expense
                                    if (!expense) showCategoryPicker = false
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label.uppercase(),
                            style = TextStyle(
                                fontFamily = SansFamily,
                                fontSize = 11.sp,
                                letterSpacing = 0.12.sp,
                                color = if (active) ColorTextPrimary else ColorTextTertiary,
                            ),
                        )
                    }
                }
            }

            // ── Amount ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ledgerCurrencySymbol(amountRowCurrency),
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontSize = 48.sp,
                        color = if (amountText.isEmpty()) ColorTextSerifMuted else ColorInk,
                    ),
                )
                BasicTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) amountText = filtered
                    },
                    textStyle = TextStyle(
                        fontFamily = SerifFamily,
                        fontSize = 48.sp,
                        color = ColorInk,
                        fontFeatureSettings = "\"tnum\" on",
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(ColorInk),
                    singleLine = true,
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box {
                            if (amountText.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = TextStyle(
                                        fontFamily = SerifFamily,
                                        fontSize = 48.sp,
                                        color = ColorTextSerifMuted,
                                        fontFeatureSettings = "\"tnum\" on",
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            if (isExpense && parsedAmount > 0 && !hasFundsForExpense) {
                val acc = selectedAccount
                val hint = if (acc.kind == AccountKind.Credit && acc.creditLimit > 0) {
                    "That would go past the credit limit you set for ${acc.name}."
                } else {
                    "That is more than the balance in ${acc.name}."
                }
                Text(
                    text = hint,
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = ColorTextSerifMuted,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }

            // ── Merchant ──────────────────────────────────────────────────
            FormField(label = "Merchant") {
                FormTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    placeholder = "Coffee shop, salary…",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
            }

            // ── Category (expense only) ────────────────────────────────────
            if (isExpense) {
                val cat = CATEGORIES[category]
                Column(modifier = Modifier.fillMaxWidth()) {
                    Caps(text = "Category", modifier = Modifier.padding(bottom = 10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { showCategoryPicker = !showCategoryPicker }
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (cat != null) {
                            IconCircle(
                                imageVector = categoryIcon(cat.icon),
                                size = 22.dp,
                                iconSize = 10.dp,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        }
                        Text(
                            text = cat?.label ?: category,
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
                        visible = showCategoryPicker,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorSurface)
                                .padding(6.dp),
                        ) {
                            RECATEGORIZABLE.entries.forEach { (key, info) ->
                                val isSelected = category == key
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ColorFeature2 else Color.Transparent)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) {
                                            haptics.tick()
                                            category = key
                                            showCategoryPicker = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconCircle(
                                        imageVector = categoryIcon(info.icon),
                                        size = 28.dp,
                                        iconSize = 12.dp,
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = info.label,
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
                }

                Spacer(Modifier.height(4.dp))
            }

            // ── Account ───────────────────────────────────────────────────
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
                                color = ColorTextSerifMuted,
                            ),
                        )
                    }
                    Hairline()
                }

                Spacer(Modifier.height(4.dp))
            }

            // ── Note ──────────────────────────────────────────────────────
            FormField(label = "Note (optional)") {
                FormTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "A moment's thought…",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Add button ────────────────────────────────────────────────
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    val actualCategory = if (isExpense) category else "income"
                    val tx = Transaction(
                        id                 = UUID.randomUUID().toString(),
                        date               = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d")),
                        time               = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")),
                        merchant           = merchant.trim(),
                        note               = note.trim().ifEmpty { "—" },
                        amount             = if (isExpense) -amount else amount,
                        category           = actualCategory,
                        icon               = CATEGORIES[actualCategory]?.icon ?: "circle",
                        account            = accounts.getOrNull(accountIdx % accounts.size)?.name ?: "",
                        recordedEpochDay   = LocalDate.now().toEpochDay(),
                        lat                = capturedLocation?.latitude,
                        lng                = capturedLocation?.longitude,
                    )
                    onAdd(tx)
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
                Caps(
                    text = "Add transaction",
                    color = if (canSubmit) ColorPage else ColorTextTertiary,
                )
            }
        }
    }
}

private fun getBestLastLocation(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return try {
        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (_: Exception) { null }
}

// ── Shared form primitives ─────────────────────────────────────────────────

@Composable
internal fun FormField(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        Caps(text = label, modifier = Modifier.padding(bottom = 10.dp))
        content()
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
internal fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontFamily = SerifFamily,
            fontSize = 16.sp,
            color = ColorInk,
        ),
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(ColorInk),
        singleLine = singleLine,
        decorationBox = { innerTextField ->
            Column {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = SerifFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 17.sp,
                                color = ColorTextSerifMuted,
                                lineHeight = (17 * 1.55).sp,
                                fontFeatureSettings = "\"tnum\" on, \"lnum\" on",
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                        )
                    }
                    innerTextField()
                }
                Hairline()
            }
        },
    )
}
