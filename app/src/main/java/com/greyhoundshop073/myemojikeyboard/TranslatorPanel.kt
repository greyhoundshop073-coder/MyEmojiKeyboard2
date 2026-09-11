package com.greyhoundshop073.myemojikeyboard

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
 * Keyboard-sized translator UI foundation.
 *
 * The panel owns language selection and request creation, while actual
 * translation is delegated to a TranslatorProvider. No fake translation is
 * shown when the provider is unavailable.
 */
class TranslatorPanel(
    private val context: Context,
    private val provider: TranslatorProvider = UnconfiguredTranslatorProvider,
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

        val translate = button("Translate")
        translate.setOnClickListener {
            pair = TranslatorLanguagePair(
                languages[sourceSpinner.selectedItemPosition],
                languages[targetSpinner.selectedItemPosition]
            )
            TranslatorPreferences.savePair(context, pair)
            val text = input.text.toString().trim()
            if (text.isEmpty()) {
                result.text = "Enter text to translate"
                return@setOnClickListener
            }
            provider.translate(TranslationRequest(text, pair))
                .onSuccess { translation ->
                    result.text = translation.translatedText
                }
                .onFailure { error ->
                    result.text = "Translation unavailable"
                    Toast.makeText(context, error.message ?: "Translation service unavailable", Toast.LENGTH_SHORT).show()
                }
        }
        root.addView(translate, LinearLayout.LayoutParams(-1, dp(46)))

        val insert = button("Insert translation")
        insert.setOnClickListener {
            val translated = result.text.toString()
            if (translated.isNotBlank() && translated != "Translation will appear here" && translated != "Translation unavailable") {
                onInsertTranslation(translated)
            }
        }
        root.addView(insert, LinearLayout.LayoutParams(-1, dp(46)))

        swap.setOnClickListener {
            val source = targetSpinner.selectedItemPosition
            val target = sourceSpinner.selectedItemPosition
            sourceSpinner.setSelection(source)
            targetSpinner.setSelection(target)
            pair = TranslatorLanguagePair(languages[source], languages[target])
            TranslatorPreferences.savePair(context, pair)
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
