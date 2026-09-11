package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import org.json.JSONArray

object SavedItemStore {

    private const val PREFS_NAME = "my_emoji_keyboard"
    private const val SAVED_ITEMS_KEY = "saved_items"
    private const val SAVED_ITEMS_LIST_KEY = "saved_items_list"

    fun getSavedItems(context: Context): List<String> {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val storedList = preferences.getString(SAVED_ITEMS_LIST_KEY, null)
        if (!storedList.isNullOrBlank()) {
            return try {
                val array = JSONArray(storedList)
                buildList(array.length()) {
                    for (index in 0 until array.length()) {
                        val item = array.optString(index)
                        if (item.isNotBlank()) add(item)
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        // Migrate the original StringSet storage once. StringSet does not guarantee
        // a stable display order, so the new JSON list is used for deterministic UI.
        val legacy = preferences.getStringSet(
            SAVED_ITEMS_KEY,
            emptySet()
        ) ?: emptySet()

        val migrated = legacy.toList()
        if (migrated.isNotEmpty()) {
            persist(preferences, migrated)
        }

        return migrated
    }

    fun saveItem(context: Context, item: String) {
        if (item.isBlank()) return

        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = getSavedItems(context).toMutableList()
        if (saved.contains(item)) return

        saved.add(item)
        persist(preferences, saved)
    }

    fun removeItem(context: Context, item: String) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = getSavedItems(context).toMutableList()
        if (!saved.remove(item)) return

        persist(preferences, saved)
    }

    fun isSaved(context: Context, item: String): Boolean {
        return getSavedItems(context).contains(item)
    }

    private fun persist(
        preferences: android.content.SharedPreferences,
        items: List<String>
    ) {
        val array = JSONArray()
        items.forEach { array.put(it) }

        preferences.edit()
            .putString(SAVED_ITEMS_LIST_KEY, array.toString())
            .remove(SAVED_ITEMS_KEY)
            .apply()
    }
}