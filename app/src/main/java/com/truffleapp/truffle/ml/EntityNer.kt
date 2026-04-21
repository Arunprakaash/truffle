package com.truffleapp.truffle.ml

import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.nl.entityextraction.MoneyEntity
import kotlinx.coroutines.tasks.await

data class NerSpan(
    val text: String,
    val label: String,
    val score: Float = 1f,
    val startChar: Int = 0,
    val endChar: Int = 0,
)

/**
 * On-device NER using ML Kit Entity Extraction.
 * Extracts MONEY and DATE_TIME entities. Model (~5MB) downloads on first use.
 * Merchant is inferred from the remaining text after removing extracted spans.
 */
class EntityNer(private val language: String = EntityExtractorOptions.ENGLISH) : AutoCloseable {

    private val extractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(language).build()
    )

    suspend fun ensureModelDownloaded() {
        extractor.downloadModelIfNeeded().await()
    }

    suspend fun predict(text: String): List<NerSpan> {
        extractor.downloadModelIfNeeded().await()

        val params = EntityExtractionParams.Builder(text).build()
        val annotations = extractor.annotate(params).await()

        val spans = mutableListOf<NerSpan>()
        val usedRanges = mutableListOf<IntRange>()

        for (annotation in annotations) {
            for (entity in annotation.entities) {
                val label = when (entity.type) {
                    Entity.TYPE_MONEY    -> "amount"
                    Entity.TYPE_DATE_TIME -> "date"
                    else                 -> continue
                }
                val spanText = text.substring(annotation.start, annotation.end)
                val nerSpan = NerSpan(
                    text      = spanText,
                    label     = label,
                    startChar = annotation.start,
                    endChar   = annotation.end,
                )
                // For amount, extract numeric value from MoneyEntity
                val finalSpan = if (entity.type == Entity.TYPE_MONEY) {
                    val money = entity.asMoneyEntity()
                    val amount = (money?.integerPart ?: 0) + (money?.fractionalPart ?: 0) / 100.0
                    nerSpan.copy(text = if (amount > 0) amount.toBigDecimal().stripTrailingZeros().toPlainString() else spanText)
                } else nerSpan

                spans.add(finalSpan)
                usedRanges.add(annotation.start until annotation.end)
            }
        }

        // Merchant heuristic: largest remaining chunk of text
        val merchant = extractMerchant(text, usedRanges)
        if (merchant.isNotBlank()) {
            spans.add(NerSpan(text = merchant, label = "merchant"))
        }

        return spans
    }

    private fun extractMerchant(text: String, usedRanges: List<IntRange>): String {
        val words = text.split(Regex("\\s+"))
        val usedChars = mutableSetOf<Int>()
        var pos = 0
        for (w in words) {
            val start = text.indexOf(w, pos)
            val end   = start + w.length
            if (usedRanges.any { r -> start >= r.first && end <= r.last + 1 }) {
                (start until end).forEach { usedChars.add(it) }
            }
            pos = end
        }
        // Collect consecutive unused chars as candidate merchant spans
        val candidates = mutableListOf<String>()
        var spanStart = -1
        for (i in text.indices) {
            if (i !in usedChars) {
                if (spanStart < 0) spanStart = i
            } else {
                if (spanStart >= 0) {
                    candidates.add(text.substring(spanStart, i).trim())
                    spanStart = -1
                }
            }
        }
        if (spanStart >= 0) candidates.add(text.substring(spanStart).trim())

        // Strip common filler words and pick longest candidate
        val fillers = setOf("at", "from", "for", "to", "the", "a", "an", "on", "in", "i", "paid", "spent", "bought")
        return candidates
            .map { c -> c.split(Regex("\\s+")).filter { it.lowercase() !in fillers }.joinToString(" ") }
            .filter { it.length > 1 }
            .maxByOrNull { it.length }
            .orEmpty()
    }

    override fun close() = extractor.close()
}
