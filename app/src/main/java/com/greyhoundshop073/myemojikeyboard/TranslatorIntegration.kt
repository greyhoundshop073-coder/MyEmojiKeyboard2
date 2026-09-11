package com.greyhoundshop073.myemojikeyboard

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Small integration boundary for the keyboard. Keeping insertion separate
 * means the existing InputMethodService remains the single owner of the
 * active InputConnection.
 *
 * Provider failures are contained here so a provider implementation cannot
 * crash the keyboard UI. Translation work is dispatched off the keyboard UI
 * thread so a network-backed provider cannot freeze keyboard interaction.
 */
class TranslatorIntegration(
    private val translatorService: TranslatorService = TranslatorService(),
    private val executor: Executor = Executors.newSingleThreadExecutor()
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
                    .onSuccess { onResult(it.translatedText) }
                    .onFailure { onError(it.message ?: "Translation unavailable") }
            }.onFailure { error ->
                onError(error.message ?: "Translation unavailable")
            }
        }
    }
}
