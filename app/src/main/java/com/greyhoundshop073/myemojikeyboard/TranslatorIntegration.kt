package com.greyhoundshop073.myemojikeyboard

/**
 * Small integration boundary for the keyboard. Keeping insertion separate
 * means the existing InputMethodService remains the single owner of the
 * active InputConnection.
 *
 * Provider failures are contained here so a provider implementation cannot
 * crash the keyboard UI. The UI receives a normal error state instead.
 */
class TranslatorIntegration(
    private val translatorService: TranslatorService = TranslatorService()
) {
    fun translate(
        text: String,
        pair: TranslatorLanguagePair,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
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
