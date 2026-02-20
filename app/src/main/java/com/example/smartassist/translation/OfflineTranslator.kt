package com.example.smartassist.translation

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OfflineTranslator {

    companion object {
        private const val TAG = "OfflineTranslator"
    }

    private var translator: Translator? = null
    private var currentTargetLang: String? = null

    /**
     * Blocking translation used inside coroutine.
     */
    suspend fun translate(
        text: String,
        targetLanguageTag: String
    ): String = withContext(Dispatchers.IO) {

        if (text.isBlank()) return@withContext text

        try {
            val mlKitLang =
                TranslateLanguage.fromLanguageTag(targetLanguageTag)
                    ?: return@withContext text

            ensureTranslatorInitialized(mlKitLang)

            translator?.translate(text)?.await() ?: text

        } catch (e: Exception) {
            Log.e(TAG, "Translation failed", e)
            text
        }
    }

    /**
     * Initialize or switch translator safely.
     */
    private suspend fun ensureTranslatorInitialized(
        targetLang: String
    ) {

        if (currentTargetLang == targetLang && translator != null) {
            return
        }

        translator?.close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLang)
            .build()

        translator = Translation.getClient(options)

        translator?.downloadModelIfNeeded(
            DownloadConditions.Builder()
                .requireWifi()
                .build()
        )?.await()

        currentTargetLang = targetLang
    }

    fun close() {
        translator?.close()
        translator = null
        currentTargetLang = null
    }
}