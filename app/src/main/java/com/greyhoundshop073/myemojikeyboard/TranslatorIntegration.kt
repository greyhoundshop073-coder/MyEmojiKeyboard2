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
 *
 * The default executor is shared because translator panels are rebuilt whenever
 * the keyboard changes mode. Creating a new executor for every panel would
 * otherwise leave a growing number of worker threads alive across mode changes.
 */
class TranslatorIntegration(
    private val translatorService: TranslatorService = TranslatorService(),
    private val executor: Executor = SharedTranslatorExecutor.executor,
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

    private object SharedTranslatorExecutor {
        val executor: Executor = Executors.newSingleThreadExecutor()
    }
}
