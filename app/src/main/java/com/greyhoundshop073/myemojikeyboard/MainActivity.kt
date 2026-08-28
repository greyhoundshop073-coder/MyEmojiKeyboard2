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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 48, 32, 48)
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
            setPadding(0, 24, 0, 32)
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
            text = "😀 Test Keyboard"
            setOnClickListener {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showInputMethodPicker()
            }
        }

        root.addView(title)
        root.addView(description)
        root.addView(settingsButton)
        root.addView(selectButton)
        root.addView(testButton)

        setContentView(root)
    }
}
