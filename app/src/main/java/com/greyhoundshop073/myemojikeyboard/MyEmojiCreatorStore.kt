package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import org.json.JSONArray

object MyEmojiCreatorStore {
    private const val PREFS = "my_emoji_creator"
    private const val KEY = "creations"

    fun getCreations(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val result = linkedSetOf<String>()
            for (i in 0 until array.length()) {
                val value = array.optString(i)
                if (value.isNotBlank()) result.add(value)
            }
            result.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, emoji: String) {
        if (emoji.isBlank()) return
        val values = getCreations(context).toMutableList()
        if (emoji in values) return
        values.add(emoji)
        persist(context, values)
    }

    fun remove(context: Context, emoji: String) {
        val values = getCreations(context).toMutableList()
        if (!values.remove(emoji)) return
        persist(context, values)
    }

    private fun persist(context: Context, values: List<String>) {
        val array = JSONArray()
        values.forEach { if (it.isNotBlank()) array.put(it) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
