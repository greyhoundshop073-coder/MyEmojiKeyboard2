package com.greyhoundshop073.myemojikeyboard

/**
 * Application-facing translation coordinator.
 *
 * Keeps the keyboard independent from a specific translation vendor. A real
 * implementation can be injected later without changing keyboard UI code.
 */
class TranslatorService(
    private val provider: TranslatorProvider = UnconfiguredTranslatorProvider
) {
    fun translate(
        text: String,
        pair: TranslatorLanguagePair
    ): Result<TranslationResult> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Translation text cannot be empty"))
        }
        if (pair.source.code == pair.target.code && pair.source.code != "auto") {
            return Result.success(TranslationResult(text, pair))
        }
        return provider.translate(TranslationRequest(text, pair))
    }
}
