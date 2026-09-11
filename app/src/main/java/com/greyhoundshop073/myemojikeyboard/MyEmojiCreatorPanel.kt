package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/** Local-only emoji mixer. It creates a reusable Unicode emoji sequence; it does not fake image generation. */
class MyEmojiCreatorPanel(
    private val context: Context,
    private val onInsert: (String) -> Unit,
    private val onClose: () -> Unit = {},
    private val onSaved: () -> Unit = {}
) {
    private val faces = listOf("😀", "😎", "🥰", "😂", "😢", "😡", "🤔", "🥳")
    private val expressions = listOf("✨", "🔥", "💯", "❤️", "💙", "💜", "💚", "⭐")
    private val accessories = listOf("👑", "🎩", "🕶️", "🎧", "🧢", "🌸", "🦋", "🚀")
    private val extras = listOf("", "👍", "🙏", "💪", "🤟", "👏", "🫶", "💎")

    private var face = faces.first()
    private var expression = expressions.first()
    private var accessory = ""
    private var extra = ""

    fun createView(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(12))
            background = rounded(Color.rgb(12, 22, 43), 18f)
        }
        root.addView(TextView(context).apply {
            text = "✦  MY EMOJI CREATOR"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, dp(42)))

        val preview = TextView(context).apply {
            textSize = 42f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(22, 38, 67), 16f)
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, dp(76)).apply {
            setMargins(0, dp(4), 0, dp(8))
        })

        fun refresh() { preview.text = buildEmoji() }
        addPicker(root, "FACE", faces) { face = it; refresh() }
        addPicker(root, "VIBE", expressions) { expression = it; refresh() }
        addPicker(root, "ACCESSORY", listOf("None") + accessories) {
            accessory = if (it == "None") "" else it
            refresh()
        }
        addPicker(root, "EXTRA", listOf("None") + extras.filter { it.isNotEmpty() }) {
            extra = if (it == "None") "" else it
            refresh()
        }

        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        actions.addView(actionButton("INSERT") { onInsert(buildEmoji()) }, actionParams())
        actions.addView(actionButton("SAVE") {
            MyEmojiCreatorStore.save(context, buildEmoji())
            onSaved()
        }, actionParams())
        actions.addView(actionButton("DONE") { onClose() }, actionParams())
        root.addView(actions, LinearLayout.LayoutParams(-1, dp(54)))
        refresh()
        return root
    }

    private fun buildEmoji(): String = face + expression + accessory + extra

    private fun addPicker(root: LinearLayout, label: String, options: List<String>, onSelect: (String) -> Unit) {
        root.addView(TextView(context).apply {
            text = label
            textSize = 10f
            setTextColor(Color.rgb(130, 210, 255))
            setPadding(dp(4), dp(4), 0, dp(2))
        }, LinearLayout.LayoutParams(-1, dp(24)))

        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = false
        }
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        options.forEach { option ->
            row.addView(Button(context).apply {
                text = option
                textSize = 22f
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = rounded(Color.rgb(24, 43, 73), 12f)
                setOnClickListener { onSelect(option) }
            }, LinearLayout.LayoutParams(dp(58), dp(46)).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            })
        }
        scroll.addView(row, LinearLayout.LayoutParams(-2, -1))
        root.addView(scroll, LinearLayout.LayoutParams(-1, dp(52)))
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(context).apply {
        text = label
        textSize = 11f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = rounded(Color.rgb(35, 55, 90), 12f)
        setOnClickListener { action() }
    }

    private fun actionParams() = LinearLayout.LayoutParams(0, -1, 1f).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }

    private fun rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Int = (value * context.resources.displayMetrics.density).toInt()
}
