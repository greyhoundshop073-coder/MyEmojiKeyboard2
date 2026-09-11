package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MyEmojiCreatorStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun saveDeduplicatesCreations() {
        val value = "😀✨👑"
        MyEmojiCreatorStore.remove(context, value)

        MyEmojiCreatorStore.save(context, value)
        MyEmojiCreatorStore.save(context, value)

        assertEquals(1, MyEmojiCreatorStore.getCreations(context).count { it == value })
        MyEmojiCreatorStore.remove(context, value)
    }

    @Test
    fun blankCreationIsIgnored() {
        val before = MyEmojiCreatorStore.getCreations(context)
        MyEmojiCreatorStore.save(context, "   ")
        assertEquals(before, MyEmojiCreatorStore.getCreations(context))
    }

    @Test
    fun removeDeletesCreation() {
        val value = "😎🔥🎧"
        MyEmojiCreatorStore.save(context, value)
        assertTrue(value in MyEmojiCreatorStore.getCreations(context))

        MyEmojiCreatorStore.remove(context, value)

        assertTrue(value !in MyEmojiCreatorStore.getCreations(context))
    }
}
