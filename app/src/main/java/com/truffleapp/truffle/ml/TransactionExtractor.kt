package com.truffleapp.truffle.ml

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ExtractedTransaction(
    val merchant: String? = null,
    val amount: Double? = null,
    val note: String? = null,
    val rawText: String = "",
)

object TransactionExtractor {

    fun extract(spans: List<NerSpan>, rawText: String): ExtractedTransaction {
        val merchant    = spans.firstOrNull { it.label == "merchant" }?.text?.trim()
        val amountText  = spans.firstOrNull { it.label == "amount"   }?.text
        val amount      = amountText?.toDoubleOrNull()

        val usedRanges  = spans.filter { it.endChar > it.startChar }.map { it.startChar..it.endChar }
        val note        = rawText
            .filterIndexed { i, _ -> usedRanges.none { i in it } }
            .trim()
            .replace(Regex("\\s+"), " ")
            .ifBlank { null }

        return ExtractedTransaction(merchant = merchant, amount = amount, note = note, rawText = rawText)
    }
}

suspend fun recognizeSpeech(context: Context): String =
    suspendCancellableCoroutine { cont ->
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val listener = object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val best = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                recognizer.destroy()
                cont.resume(best)
            }
            override fun onError(error: Int) {
                recognizer.destroy()
                cont.resumeWithException(RuntimeException("SpeechRecognizer error $error"))
            }
            override fun onReadyForSpeech(p: Bundle?) = Unit
            override fun onBeginningOfSpeech()        = Unit
            override fun onRmsChanged(v: Float)       = Unit
            override fun onBufferReceived(b: ByteArray?) = Unit
            override fun onEndOfSpeech()              = Unit
            override fun onPartialResults(p: Bundle?) = Unit
            override fun onEvent(t: Int, p: Bundle?)  = Unit
        }

        recognizer.setRecognitionListener(listener)
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
        )
        cont.invokeOnCancellation { recognizer.destroy() }
    }
