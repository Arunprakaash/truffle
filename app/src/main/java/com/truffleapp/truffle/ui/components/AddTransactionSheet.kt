package com.truffleapp.truffle.ui.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.Account
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

    var amountText       by remember { mutableStateOf("") }
    var isExpense        by remember { mutableStateOf(true) }
    var merchant         by remember { mutableStateOf("") }
    var category         by remember { mutableStateOf("food") }
    var note             by remember { mutableStateOf("") }
    var accountIdx       by remember { mutableIntStateOf(0) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val ledgerDc = normalizeLedgerCurrencyCode(displayCurrency)
    val amountRowCurrency = remember(ledgerDc, accountIdx, accounts) {
        if (accounts.isEmpty()) ledgerDc
        else normalizeLedgerCurrencyCode(accounts[accountIdx % accounts.size].currency)
    }

    val canSubmit = (amountText.toDoubleOrNull() ?: 0.0) > 0 && merchant.isNotBlank()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                                isExpense = expense
                                if (!expense) showCategoryPicker = false
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

            Spacer(Modifier.height(8.dp))

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
                                fontSize = 16.sp,
                                color = ColorTextSerifMuted,
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
