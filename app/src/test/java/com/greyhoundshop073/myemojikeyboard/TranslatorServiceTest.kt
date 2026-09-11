package com.greyhoundshop073.myemojikeyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslatorServiceTest {

    private val english = TranslatorLanguage("en", "English")
    private val yoruba = TranslatorLanguage("yo", "Yoruba")

    @Test
    fun blankTextIsRejectedBeforeProviderCall() {
        var providerCalled = false
        val provider = TranslatorProvider { 
            providerCalled = true
            Result.success(TranslationResult("unused", TranslatorLanguagePair(english, yoruba)))
        }

        val result = TranslatorService(provider).translate(
            "   ",
            TranslatorLanguagePair(english, yoruba)
        )

        assertTrue(result.isFailure)
        assertTrue(!providerCalled)
    }

    @Test
    fun sameLanguageReturnsOriginalTextWithoutProviderCall() {
        var providerCalled = false
        val provider = TranslatorProvider {
            providerCalled = true
            Result.failure(IllegalStateException("provider should not be called"))
        }
        val pair = TranslatorLanguagePair(english, english)

        val result = TranslatorService(provider).translate("Hello", pair)

        assertTrue(result.isSuccess)
        assertEquals("Hello", result.getOrThrow().translatedText)
        assertTrue(!providerCalled)
    }

    @Test
    fun differentLanguageDelegatesToProvider() {
        var request: TranslationRequest? = null
        val provider = TranslatorProvider {
            request = it
            Result.success(TranslationResult("Bawo", it.pair))
        }
        val pair = TranslatorLanguagePair(english, yoruba)

        val result = TranslatorService(provider).translate("Hello", pair)

        assertTrue(result.isSuccess)
        assertEquals("Hello", request?.text)
        assertEquals(pair, request?.pair)
        assertEquals("Bawo", result.getOrThrow().translatedText)
    }
}
