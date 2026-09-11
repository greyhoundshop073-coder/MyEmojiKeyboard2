package com.greyhoundshop073.myemojikeyboard

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Small integration boundary for the keyboard. Keeping insertion separate
 * means the existing InputMethodService remains the single owner of the
 * active InputConnection.
 *
 * Provider failures are contained here so a provider implementation cannot
 * crash the keyboard UI. Translation work is dispatched off the keyboard UI
 * thread, while results are delivered back on the main thread for safe UI
 * updates.
 */
class TranslatorIntegration(
    private val translatorService: TranslatorService = TranslatorService(),
    private val executor: Executor = Executors.newSingleThreadExecutor(),
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
) {
    fun translate(
        text: String,
        pair: TranslatorLanguagePair,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            runCatching {
                translatorService.translate(text, pair)
            }.onSuccess { result ->
                result
                    .onSuccess { translation ->
                        mainHandler.post { onResult(translation.translatedText) }
                    }
                    .onFailure { error ->
                        mainHandler.post { onError(error.message ?: "Translation unavailable") }
                    }
            }.onFailure { error ->
                mainHandler.post { onError(error.message ?: "Translation unavailable") }
            }
        }
    }
}
