package com.greyhoundshop073.myemojikeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(32), dp(48), dp(32), dp(48))
        }

        val title = TextView(this).apply {
            text = "😀 My Emoji Keyboard"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            text = """
                Your emoji keyboard is ready.

                Use the buttons below to enable and select My Emoji Keyboard.

                Once selected, open WhatsApp, Telegram, Messages, or another app and tap a text field to use your emoji keyboard.
            """.trimIndent()
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(20))
        }

        status = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24))
        }

        val settingsButton = Button(this).apply {
            text = "⚙️ Enable Keyboard"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val selectButton = Button(this).apply {
            text = "⌨️ Select My Emoji Keyboard"
            setOnClickListener {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showInputMethodPicker()
            }
        }

        val testButton = Button(this).apply {
            text = "😀 Open Keyboard Picker"
            setOnClickListener {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showInputMethodPicker()
            }
        }

        root.addView(title)
        root.addView(description)
        root.addView(status)
        root.addView(settingsButton)
        root.addView(selectButton)
        root.addView(testButton)

        setContentView(root)
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun updateStatus() {
        val packageName = "$packageName/com.greyhoundshop073.myemojikeyboard.MyEmojiInputMethodService"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
            ?.split(":")
            ?.any { it == packageName }
            == true
        val selected = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) == packageName

        status.text = when {
            selected -> "✅ My Emoji Keyboard is enabled and selected"
            enabled -> "🟡 My Emoji Keyboard is enabled — select it as your keyboard"
            else -> "⚪ My Emoji Keyboard is not enabled yet"
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
