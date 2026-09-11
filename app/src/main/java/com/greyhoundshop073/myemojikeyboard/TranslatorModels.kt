package com.greyhoundshop073.myemojikeyboard

/**
 * Language pair used by the keyboard translator.
 *
 * This file deliberately contains no translation provider or API secret.
 * The keyboard can use these models locally while a secure backend/provider
 * is connected later.
 */
data class TranslatorLanguage(
    val code: String,
    val displayName: String
)

data class TranslatorLanguagePair(
    val source: TranslatorLanguage,
    val target: TranslatorLanguage
)

data class TranslationRequest(
    val text: String,
    val pair: TranslatorLanguagePair
)

data class TranslationResult(
    val translatedText: String,
    val pair: TranslatorLanguagePair
)

object TranslatorLanguages {
    val defaults = listOf(
        TranslatorLanguage("auto", "Detect language"),
        TranslatorLanguage("en", "English"),
        TranslatorLanguage("ig", "Igbo"),
        TranslatorLanguage("yo", "Yoruba"),
        TranslatorLanguage("ha", "Hausa"),
        TranslatorLanguage("fr", "French"),
        TranslatorLanguage("es", "Spanish"),
        TranslatorLanguage("de", "German"),
        TranslatorLanguage("pt", "Portuguese"),
        TranslatorLanguage("ar", "Arabic"),
        TranslatorLanguage("zh", "Chinese"),
        TranslatorLanguage("ja", "Japanese"),
        TranslatorLanguage("ko", "Korean")
    )

    fun find(code: String): TranslatorLanguage =
        defaults.firstOrNull { it.code == code } ?: defaults[1]
}

/**
 * Translation provider boundary.
 *
 * Implementations must return a real result or an explicit failure. The
 * keyboard must never pretend that untranslated text is a successful result.
 */
interface TranslatorProvider {
    fun translate(request: TranslationRequest): Result<TranslationResult>
}

/**
 * Safe placeholder provider used until a real provider is connected.
 * It never fabricates a translation.
 */
object UnconfiguredTranslatorProvider : TranslatorProvider {
    override fun translate(request: TranslationRequest): Result<TranslationResult> =
        Result.failure(IllegalStateException("Translation service is not configured"))
}
