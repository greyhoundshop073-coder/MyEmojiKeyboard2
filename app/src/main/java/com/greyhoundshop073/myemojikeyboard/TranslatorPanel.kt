package com.greyhoundshop073.myemojikeyboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

/**
 * Keyboard-sized translator UI.
 *
 * The panel talks only to TranslatorIntegration. A real provider can therefore
 * be added later without changing the keyboard UI or exposing API secrets in
 * the APK.
 */
class TranslatorPanel(
    private val context: Context,
    private val integration: TranslatorIntegration = TranslatorIntegration(),
    private val initialText: String = "",
    private val onInsertTranslation: (String) -> Unit
) {
    private val languages = TranslatorLanguages.defaults
    private var pair = TranslatorPreferences.loadPair(context)

    fun createView(): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = gradient(intArrayOf(Color.rgb(8, 17, 37), Color.rgb(29, 13, 51)), 20f)
        }

        val title = TextView(context).apply {
            text = "🌐  Translator"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(title, LinearLayout.LayoutParams(-1, dp(38)))

        val languageRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val sourceSpinner = spinner(pair.source)
        val targetSpinner = spinner(pair.target)
        val swap = button("⇄")

        languageRow.addView(sourceSpinner, LinearLayout.LayoutParams(0, dp(46), 1f))
        languageRow.addView(swap, LinearLayout.LayoutParams(dp(50), dp(46)))
        languageRow.addView(targetSpinner, LinearLayout.LayoutParams(0, dp(46), 1f))
        root.addView(languageRow)

        val input = EditText(context).apply {
            hint = "Type or paste text to translate"
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(145, 165, 195))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = gradient(intArrayOf(Color.rgb(18, 31, 55), Color.rgb(24, 30, 59)), 14f)
            setText(initialText)
            setSelection(length())
            minLines = 2
            maxLines = 4
            isSingleLine = false
        }
        root.addView(input, LinearLayout.LayoutParams(-1, dp(74)))

        val result = TextView(context).apply {
            text = "Translation will appear here"
            textSize = 15f
            setTextColor(Color.rgb(190, 220, 255))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = gradient(intArrayOf(Color.rgb(13, 27, 48), Color.rgb(27, 20, 54)), 14f)
        }
        root.addView(result, LinearLayout.LayoutParams(-1, dp(74)))

        var translatedText: String? = null
        val insert = button("Insert translation")
        insert.isEnabled = false
        val copy = button("Copy")
        copy.isEnabled = false
        val clearInput = button("Clear")

        val inputActions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        inputActions.addView(clearInput, LinearLayout.LayoutParams(-1, dp(40)))
        root.addView(inputActions)

        val resultActions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        resultActions.addView(copy, LinearLayout.LayoutParams(0, dp(40), 1f))
        resultActions.addView(insert, LinearLayout.LayoutParams(0, dp(40), 1f))
        root.addView(resultActions)

        clearInput.setOnClickListener {
            input.text.clear()
            translatedText = null
            insert.isEnabled = false
            copy.isEnabled = false
            result.text = "Translation will appear here"
        }

        val translate = button("Translate")
        translate.setOnClickListener {
            pair = TranslatorLanguagePair(
                languages[sourceSpinner.selectedItemPosition],
                languages[targetSpinner.selectedItemPosition]
            )
            TranslatorPreferences.savePair(context, pair)
            val text = input.text.toString().trim()
            if (text.isEmpty()) {
                translatedText = null
                insert.isEnabled = false
                copy.isEnabled = false
                result.text = "Enter text to translate"
                return@setOnClickListener
            }

            translatedText = null
            insert.isEnabled = false
            copy.isEnabled = false
            result.text = "Translating…"
            integration.translate(
                text = text,
                pair = pair,
                onResult = { translation ->
                    translatedText = translation
                    result.text = translation
                    insert.isEnabled = translation.isNotBlank()
                    copy.isEnabled = translation.isNotBlank()
                },
                onError = { error ->
                    translatedText = null
                    insert.isEnabled = false
                    copy.isEnabled = false
                    result.text = "Translation unavailable"
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
        root.addView(translate, LinearLayout.LayoutParams(-1, dp(46)))

        copy.setOnClickListener {
            translatedText?.takeIf { it.isNotBlank() }?.let { translation ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Translation", translation))
                Toast.makeText(context, "Translation copied", Toast.LENGTH_SHORT).show()
            }
        }

        insert.setOnClickListener {
            translatedText?.takeIf { it.isNotBlank() }?.let(onInsertTranslation)
        }

        swap.setOnClickListener {
            val source = targetSpinner.selectedItemPosition
            val target = sourceSpinner.selectedItemPosition
            sourceSpinner.setSelection(source)
            targetSpinner.setSelection(target)
            pair = TranslatorLanguagePair(languages[source], languages[target])
            TranslatorPreferences.savePair(context, pair)
            translatedText = null
            insert.isEnabled = false
            copy.isEnabled = false
            result.text = "Translation will appear here"
        }

        return root
    }

    private fun spinner(selected: TranslatorLanguage): Spinner = Spinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, languages.map { it.displayName })
        setSelection(languages.indexOfFirst { it.code == selected.code }.coerceAtLeast(0))
    }

    private fun button(label: String): Button = Button(context).apply {
        text = label
        textSize = 12f
        setTextColor(Color.WHITE)
        background = gradient(intArrayOf(Color.rgb(0, 145, 220), Color.rgb(116, 55, 230)), 14f)
    }

    private fun gradient(colors: IntArray, radius: Float) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        colors
    ).apply { cornerRadius = radius }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
