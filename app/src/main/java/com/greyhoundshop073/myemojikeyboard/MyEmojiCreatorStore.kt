package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import org.json.JSONArray

object MyEmojiCreatorStore {
    private const val PREFS = "my_emoji_creator"
    private const val KEY = "creations"
    private const val MAX_CREATIONS = 100
    private const val MAX_EMOJI_LENGTH = 64

    fun getCreations(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val result = linkedSetOf<String>()
            for (i in 0 until array.length()) {
                val value = array.optString(i).trim()
                if (value.isNotEmpty() && value.length <= MAX_EMOJI_LENGTH) {
                    result.add(value)
                    if (result.size == MAX_CREATIONS) break
                }
            }
            result.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, emoji: String) {
        val value = emoji.trim()
        if (value.isEmpty() || value.length > MAX_EMOJI_LENGTH) return
        val values = getCreations(context).toMutableList()
        if (value in values) return
        if (values.size >= MAX_CREATIONS) values.removeAt(0)
        values.add(value)
        persist(context, values)
    }

    fun remove(context: Context, emoji: String) {
        val values = getCreations(context).toMutableList()
        if (!values.remove(emoji)) return
        persist(context, values)
    }

    private fun persist(context: Context, values: List<String>) {
        val array = JSONArray()
        values.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length <= MAX_EMOJI_LENGTH }
            .distinct()
            .take(MAX_CREATIONS)
            .forEach { array.put(it) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
