package com.greyhoundshop073.myemojikeyboard

import android.content.Context

/**
 * Stores only the user's last language-pair choice on-device.
 * No translated text is persisted here.
 */
object TranslatorPreferences {
    private const val PREFS = "translator_preferences"
    private const val SOURCE = "source_language"
    private const val TARGET = "target_language"

    fun loadPair(context: Context): TranslatorLanguagePair {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val source = TranslatorLanguages.find(prefs.getString(SOURCE, "auto") ?: "auto")
        val target = TranslatorLanguages.find(prefs.getString(TARGET, "en") ?: "en")
        return TranslatorLanguagePair(source, target)
    }

    fun savePair(context: Context, pair: TranslatorLanguagePair) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SOURCE, pair.source.code)
            .putString(TARGET, pair.target.code)
            .apply()
    }
}
