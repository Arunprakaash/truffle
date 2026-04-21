package com.truffleapp.truffle.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.truffleapp.truffle.data.CATEGORIES
import com.truffleapp.truffle.data.RECATEGORIZABLE
import com.truffleapp.truffle.data.Transaction
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.SansFamily
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.tan

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxDetailSheet(
    tx: Transaction,
    currencyCode: String,
    transactions: List<Transaction> = emptyList(),
    onDismiss: () -> Unit,
    onRecategorize: (txId: String, category: String) -> Unit,
    onRemove: (txId: String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = rememberHaptics()
    var currentCategory by remember { mutableStateOf(tx.category) }
    var showCats by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val cat = CATEGORIES[currentCategory] ?: CATEGORIES[tx.category]

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

            // Header row: date/merchant + close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Caps(
                        text = "${tx.date}, ${tx.time}",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = tx.merchant,
                        style = TextStyle(
                            fontFamily = SerifFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = ColorInk,
                        ),
                    )
                }
                // close button
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ColorSurface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(14.dp),
                        tint = ColorInk,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Amount
            MoneyText(
                amount = tx.amount,
                currencyCode = currencyCode,
                size = 40.sp,
                cents = true,
                sign = true,
            )

            Spacer(Modifier.height(10.dp))

            // Detail rows
            DetailRow(label = "Paid from", value = tx.account)
            DetailRow(
                label = "Category",
                value = cat?.label ?: tx.category,
                icon = cat?.icon,
                onClick = { showCats = !showCats },
                chevron = true,
            )

            // Category picker
            AnimatedVisibility(
                visible = showCats,
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
                        val isSelected = currentCategory == key
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
                                    currentCategory = key
                                    onRecategorize(tx.id, key)
                                    showCats = false
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

            DetailRow(label = "Status", value = "Cleared")

            Hairline(modifier = Modifier.padding(top = 12.dp, bottom = 20.dp))

            // Reflection note
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorSurface)
                    .padding(16.dp),
            ) {
                Caps(text = "A note", modifier = Modifier.padding(bottom = 8.dp))
                val noteText = detailSheetNote(tx, transactions, currencyCode)
                Text(
                    text = noteText,
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp,
                        color = ColorTextSerifMuted,
                        lineHeight = (17 * 1.55).sp,
                    ),
                )
            }

            // ── Location map ──────────────────────────────────────────────
            if (tx.lat != null && tx.lng != null) {
                Spacer(Modifier.height(12.dp))
                val context = LocalContext.current
                val zoom = 16
                val tile = latLngToTileInfo(tx.lat, tx.lng, zoom)
                val mapTone = remember {
                    ColorMatrix().apply { setToSaturation(0f) }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val tileSize = maxWidth / 3

                    // Container is exactly one tile tall so the centre row fills it perfectly.
                    // Rounded corners only clip the tile corners, never the map content itself.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tileSize)
                            .clip(RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        // 3×3 tile grid — overflow top/bottom, clipped to one tile height.
                        Column {
                            for (dy in -1..1) {
                                Row {
                                    for (dx in -1..1) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data("https://tile.openstreetmap.org/$zoom/${tile.x + dx}/${tile.y + dy}.png")
                                                .addHeader("User-Agent", "Truffle/1.2 personal-finance-app")
                                                .crossfade(false)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            colorFilter = ColorFilter.colorMatrix(mapTone),
                                            alpha = 0.86f,
                                            modifier = Modifier.size(tileSize),
                                        )
                                    }
                                }
                            }
                        }

                        // Gentle warm overlay to keep map in Stillwater palette.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ColorPage.copy(alpha = 0.28f)),
                        )

                        // Pin at exact sub-tile position.
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val gridWidth = (tileSize * 3).toPx()
                            val gridHeight = (tileSize * 3).toPx()
                            val originX = (size.width - gridWidth) / 2f
                            val originY = (size.height - gridHeight) / 2f
                            val pinX = originX + (tileSize * (1 + tile.fracX)).toPx()
                            val pinY = originY + (tileSize * (1 + tile.fracY)).toPx()
                            val center = androidx.compose.ui.geometry.Offset(pinX, pinY)
                            drawCircle(color = ColorPage, radius = 10.dp.toPx(), center = center)
                            drawCircle(color = ColorInk, radius = 6.dp.toPx(), center = center)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SheetButton(
                icon = Icons.Outlined.DeleteForever,
                text = "Delete",
                onClick = { showDeleteConfirm = true },
                variant = SheetButtonVariant.Destructive,
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Remove this transaction?",
                    style = TextStyle(fontFamily = SerifFamily, fontSize = 18.sp, color = ColorInk),
                )
            },
            text = {
                Text(
                    text = "It will disappear from Flow and Today, and the linked account balance will move back as if it never happened.",
                    style = TextStyle(
                        fontFamily = SerifFamily,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 14.sp,
                        color      = ColorTextSerifBody,
                        lineHeight = (14 * 1.55).sp,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.heavyClick()
                        showDeleteConfirm = false
                        onRemove(tx.id)
                        onDismiss()
                    },
                ) {
                    Text("Remove", style = TextStyle(fontFamily = SansFamily, color = ColorInk))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", style = TextStyle(fontFamily = SansFamily, color = ColorTextTertiary))
                }
            },
        )
    }
}

// ── DetailRow ─────────────────────────────────────────────────────────────────
@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: String? = null,
    onClick: (() -> Unit)? = null,
    chevron: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Caps(text = label, modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconCircle(
                    imageVector = categoryIcon(icon),
                    size = 22.dp,
                    iconSize = 10.dp,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = ColorInk,
                ),
            )
            if (chevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(10.dp),
                    tint = ColorTextTertiary,
                )
            }
        }
    }
    Hairline()
}

private fun transactionCalendarMonth(tx: Transaction): YearMonth? =
    if (tx.recordedEpochDay > 0L) YearMonth.from(LocalDate.ofEpochDay(tx.recordedEpochDay))
    else null

private fun monthHeading(ym: YearMonth): String =
    ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

private fun detailSheetNote(tx: Transaction, transactions: List<Transaction>, currencyCode: String): String {
    val ym = transactionCalendarMonth(tx)
    val catLabel = CATEGORIES[tx.category]?.label ?: tx.category

    if (tx.amount < 0.0) {
        if (ym == null) {
            return "This ${fmt(-tx.amount, currencyCode = currencyCode, cents = true)} went to $catLabel."
        }
        val monthTxs = transactions.filter { t ->
            t.amount < 0.0 &&
                t.category == tx.category &&
                t.recordedEpochDay > 0L &&
                YearMonth.from(LocalDate.ofEpochDay(t.recordedEpochDay)) == ym
        }
        val totalOut = monthTxs.sumOf { -it.amount }
        val n = monthTxs.size
        val heading = monthHeading(ym)
        return when {
            n <= 1 ->
                "In $heading, this was your only $catLabel spend."
            else ->
                "In $heading, you spent ${fmt(totalOut, currencyCode = currencyCode, cents = true)} on $catLabel across $n transactions."
        }
    }

    if (ym == null) {
        return "This inflow of ${fmt(tx.amount, currencyCode = currencyCode, cents = true)} came from ${tx.merchant}."
    }
    val monthIn = transactions.filter { t ->
        t.amount > 0.0 &&
            t.category == tx.category &&
            t.recordedEpochDay > 0L &&
            YearMonth.from(LocalDate.ofEpochDay(t.recordedEpochDay)) == ym
    }
    val totalIn = monthIn.sumOf { it.amount }
    val n = monthIn.size
    val heading = monthHeading(ym)
    return when {
        n <= 1 ->
            "In $heading, this was ${fmt(tx.amount, currencyCode = currencyCode, cents = true)} from ${tx.merchant}."
        else ->
            "In $heading, you received ${fmt(totalIn, currencyCode = currencyCode, cents = true)} across $n deposits."
    }
}

private data class TileInfo(val x: Int, val y: Int, val fracX: Float, val fracY: Float)

private fun latLngToTileInfo(lat: Double, lng: Double, zoom: Int): TileInfo {
    val n = Math.pow(2.0, zoom.toDouble())
    val xf = (lng + 180) / 360 * n
    val latRad = Math.toRadians(lat)
    val yf = (1 - ln(tan(latRad) + 1.0 / cos(latRad)) / Math.PI) / 2 * n
    return TileInfo(
        x = xf.toInt(),
        y = yf.toInt(),
        fracX = (xf - xf.toInt()).toFloat(),
        fracY = (yf - yf.toInt()).toFloat(),
    )
}
