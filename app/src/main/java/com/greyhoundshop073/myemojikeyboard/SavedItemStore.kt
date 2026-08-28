package com.greyhoundshop073.myemojikeyboard

import android.content.Context

object SavedItemStore {

    private const val PREFS_NAME = "my_emoji_keyboard"
    private const val SAVED_ITEMS_KEY = "saved_items"

    fun getSavedItems(context: Context): List<String> {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = preferences.getStringSet(
            SAVED_ITEMS_KEY,
            emptySet()
        ) ?: emptySet()

        return saved.toList()
    }

    fun saveItem(context: Context, item: String) {
        if (item.isBlank()) return

        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = preferences.getStringSet(
            SAVED_ITEMS_KEY,
            emptySet()
        )?.toMutableSet() ?: mutableSetOf()

        saved.add(item)

        preferences.edit()
            .putStringSet(SAVED_ITEMS_KEY, saved)
            .apply()
    }

    fun removeItem(context: Context, item: String) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val saved = preferences.getStringSet(
            SAVED_ITEMS_KEY,
            emptySet()
        )?.toMutableSet() ?: mutableSetOf()

        saved.remove(item)

        preferences.edit()
            .putStringSet(SAVED_ITEMS_KEY, saved)
            .apply()
    }

    fun isSaved(context: Context, item: String): Boolean {
        return getSavedItems(context).contains(item)
    }
}
