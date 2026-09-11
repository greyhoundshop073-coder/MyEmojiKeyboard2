package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import org.json.JSONArray

object SavedItemStore {

    private const val PREFS_NAME = "my_emoji_keyboard"
    private const val SAVED_ITEMS_KEY = "saved_items"
    private const val SAVED_ITEMS_LIST_KEY = "saved_items_list"
    private const val MAX_SAVED_ITEMS = 200
    private const val MAX_ITEM_LENGTH = 512

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
                    val item = array.optString(index).trim()
                    if (item.isNotBlank() && item.length <= MAX_ITEM_LENGTH) {
                        items.add(item)
                        if (items.size == MAX_SAVED_ITEMS) break
                    }
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
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length <= MAX_ITEM_LENGTH }
            .distinct()
            .sorted()
            .take(MAX_SAVED_ITEMS)
            .toList()

        if (migrated.isNotEmpty()) {
            persist(preferences, migrated)
        }

        return migrated
    }

    fun saveItem(context: Context, item: String) {
        val value = item.trim()
        if (value.isBlank() || value.length > MAX_ITEM_LENGTH) return

        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = getSavedItems(context).toMutableList()
        if (saved.contains(value)) return

        if (saved.size >= MAX_SAVED_ITEMS) {
            saved.removeAt(0)
        }
        saved.add(value)
        persist(preferences, saved)
    }

    fun removeItem(context: Context, item: String) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = getSavedItems(context).toMutableList()
        if (!saved.remove(item.trim())) return

        persist(preferences, saved)
    }

    fun isSaved(context: Context, item: String): Boolean {
        return getSavedItems(context).contains(item.trim())
    }

    private fun itemsFromJson(storedList: String): List<String> {
        val array = JSONArray(storedList)
        return buildList(minOf(array.length(), MAX_SAVED_ITEMS)) {
            for (index in 0 until array.length()) {
                val item = array.optString(index).trim()
                if (item.isNotBlank() && item.length <= MAX_ITEM_LENGTH && !contains(item)) {
                    add(item)
                }
                if (size == MAX_SAVED_ITEMS) break
            }
        }
    }

    private fun persist(
        preferences: android.content.SharedPreferences,
        items: List<String>
    ) {
        val array = JSONArray()
        items.asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length <= MAX_ITEM_LENGTH }
            .distinct()
            .take(MAX_SAVED_ITEMS)
            .forEach { array.put(it) }

        preferences.edit()
            .putString(SAVED_ITEMS_LIST_KEY, array.toString())
            .remove(SAVED_ITEMS_KEY)
            .apply()
    }
}