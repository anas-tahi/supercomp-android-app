package com.supercomp.android.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class VoiceToTextParser(private val context: Context) : RecognitionListener {
    var onResult: (String) -> Unit = {}
    var onError: (String) -> Unit = {}
    var onStateChange: (Boolean) -> Unit = {}

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault())
            putExtra(
                "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                arrayOf<String>() // Google decides based on context
            )
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false) // use online for better accuracy
        }
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
        onStateChange(true)
    }

    fun stopListening() {
        recognizer.stopListening()
        onStateChange(false)
    }

    override fun onResults(results: Bundle?) {
        val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        data?.get(0)?.let { onResult(it) }
        onStateChange(false)
    }

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        onError(message)
        onStateChange(false)
        android.util.Log.e("VoiceToText", "Error code: $error — $message")
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        recognizer.destroy()
    }
}

@Composable
fun rememberVoiceToTextParser(): VoiceToTextParser {
    val context = LocalContext.current
    val parser = remember { VoiceToTextParser(context) }
    DisposableEffect(Unit) {
        onDispose { parser.destroy() }
    }
    return parser
}