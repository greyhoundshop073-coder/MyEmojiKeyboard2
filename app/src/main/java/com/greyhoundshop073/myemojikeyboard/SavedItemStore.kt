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
            try {
                val array = JSONArray(storedList)
                val items = linkedSetOf<String>()
                for (index in 0 until array.length()) {
                    val item = array.optString(index)
                    if (item.isNotBlank()) items.add(item)
                }
                val recovered = items.toList()
                if (recovered != itemsFromJson(storedList)) {
                    persist(preferences, recovered)
                }
                return recovered
            } catch (_: Exception) {
                // Fall through to the legacy store so a damaged JSON value
                // does not permanently hide previously saved items.
            }
        }

        // Migrate the original StringSet storage once. StringSet does not guarantee
        // a stable display order, so sort the migrated values before persisting them.
        val legacy = preferences.getStringSet(
            SAVED_ITEMS_KEY,
            emptySet()
        ) ?: emptySet()

        val migrated = legacy
            .filter { it.isNotBlank() }
            .sorted()

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

    private fun itemsFromJson(storedList: String): List<String> {
        val array = JSONArray(storedList)
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                val item = array.optString(index)
                if (item.isNotBlank()) add(item)
            }
        }
    }

    private fun persist(
        preferences: android.content.SharedPreferences,
        items: List<String>
    ) {
        val array = JSONArray()
        items.forEach { item ->
            if (item.isNotBlank()) array.put(item)
        }

        preferences.edit()
            .putString(SAVED_ITEMS_LIST_KEY, array.toString())
            .remove(SAVED_ITEMS_KEY)
            .apply()
    }
}