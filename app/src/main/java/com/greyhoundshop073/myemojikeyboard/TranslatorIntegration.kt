package com.greyhoundshop073.myemojikeyboard

/**
 * Small integration boundary for the keyboard. Keeping insertion separate
 * means the existing InputMethodService remains the single owner of the
 * active InputConnection.
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
        translatorService.translate(text, pair)
            .onSuccess { onResult(it.translatedText) }
            .onFailure { onError(it.message ?: "Translation unavailable") }
    }
}
