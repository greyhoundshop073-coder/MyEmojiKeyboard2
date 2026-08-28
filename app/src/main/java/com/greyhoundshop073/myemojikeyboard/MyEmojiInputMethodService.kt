package com.greyhoundshop073.myemojikeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MyEmojiInputMethodService : InputMethodService() {

    private lateinit var emojiContainer: LinearLayout
    private var currentCategory = "Smileys"

    private val categories = mapOf(
        "Smileys" to listOf(
            "😀","😃","😄","😁","😆","😅","😂","🤣",
            "😊","😇","🙂","🙃","😉","😌","😍","🥰",
            "😘","😗","😙","😚","😋","😛","😝","😜",
            "🤪","🤨","🧐","🤓","😎","🤩","🥳","😏",
            "😒","😞","😔","😟","😕","🙁","☹️","😣",
            "😖","😫","😩","🥺","😢","😭","😤","😠",
            "😡","🤬","🤯","😳","🥵","🥶","😱","😨",
            "😰","😥","😓","🤗","🤔","🫣","🤭","🫢",
            "🤫","🤥","😶","🫠","😐","😑","😬","🙄",
            "😯","😦","😧","😮","😲","🥱","😴","🤤"
        ),
        "People" to listOf(
            "👋","🤚","🖐️","✋","🖖","👌","🤏","✌️",
            "🤞","🤟","🤘","🤙","👈","👉","👆","👇",
            "☝️","👍","👎","✊","👊","🤲","👏","🙌",
            "👐","🤝","🙏","✍️","💅","🤳","💪","🫶",
            "👶","🧒","👦","👧","🧑","👱","👨","👩",
            "🧔","👵","👴","🙍","🙎","🙅","🙆","💁",
            "🙋","🧏","🙇","🤦","🤷","👮","👷","💂"
        ),
        "Hearts" to listOf(
            "❤️","🧡","💛","💚","💙","💜","🖤","🩷",
            "🩵","🩶","🤍","🤎","💔","❤️‍🔥","❤️‍🩹","❣️",
            "💕","💞","💓","💗","💖","💘","💝","💟",
            "💌","💋","💯","💢","💥","💫","💦","💨"
        ),
        "Animals" to listOf(
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼",
            "🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈",
            "🙉","🙊","🐔","🐧","🐦","🐤","🐣","🦆",
            "🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝",
            "🐛","🦋","🐌","🐞","🐜","🪲","🕷️","🦂",
            "🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦀",
            "🐠","🐟","🐡","🦈","🐬","🐳","🐋","🦭"
        ),
        "Food" to listOf(
            "🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓",
            "🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝",
            "🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑",
            "🌽","🥕","🧄","🧅","🥔","🍞","🥐","🥖",
            "🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩",
            "🍗","🍔","🍟","🍕","🌭","🌮","🌯","🥗",
            "🍿","🍩","🍪","🎂","🍰","🧁","🍫","🍭",
            "☕","🧃","🥤","🧋","🍵"
        ),
        "Symbols" to listOf(
            "⭐","🌟","✨","💫","🔥","💎","👑","🎯",
            "✅","❌","❗","❓","‼️","⁉️","⚠️","⭕",
            "❌","➕","➖","✖️","➗","♾️","💯","©️",
            "®️","™️","✔️","☑️","🔴","🟠","🟡","🟢",
            "🔵","🟣","⚫","⚪","🟤","🔷","🔶","🔺",
            "🔻","🔰","♻️","⚡","☀️","☁️","☂️","☮️",
            "☯️","✝️","☪️","🕉️","☸️","✡️","🔱","⚜️"
        )
    )

    override fun onCreateInputView(): View {
        return createKeyboard()
    }

    private fun createKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(8, 8, 8, 8)
        }

        val title = TextView(this).apply {
            text = "😀  My Emoji Keyboard"
            textSize = 18f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 12)
        }
        root.addView(title)

        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        categories.keys.forEach { category ->
            val button = Button(this).apply {
                text = category
                textSize = 12f
                setOnClickListener {
                    currentCategory = category
                    displayCategory(category)
                }
            }
            categoryRow.addView(
                button,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        categoryScroll.addView(categoryRow)
        root.addView(
            categoryScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val scrollView = ScrollView(this)
        emojiContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        scrollView.addView(
            emojiContainer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val spaceButton = Button(this).apply {
            text = "SPACE"
            setOnClickListener {
                currentInputConnection?.commitText(" ", 1)
            }
        }

        val deleteButton = Button(this).apply {
            text = "⌫"
            textSize = 20f
            setOnClickListener {
                deletePreviousCharacter()
            }
        }

        val enterButton = Button(this).apply {
            text = "↵"
            textSize = 20f
            setOnClickListener {
                currentInputConnection?.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_ENTER
                    )
                )
                currentInputConnection?.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_ENTER
                    )
                )
            }
        }

        bottomRow.addView(
            spaceButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        bottomRow.addView(
            deleteButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f)
        )
        bottomRow.addView(
            enterButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f)
        )

        root.addView(bottomRow)

        displayCategory("Smileys")

        return root
    }

    private fun displayCategory(category: String) {
        emojiContainer.removeAllViews()

        val emojis = categories[category] ?: emptyList()

        var row: LinearLayout? = null

        emojis.forEachIndexed { index, emoji ->
            if (index % 8 == 0) {
                row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                }
                emojiContainer.addView(
                    row,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            val button = TextView(this).apply {
                text = emoji
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(8, 10, 8, 10)
                setOnClickListener {
                    currentInputConnection?.commitText(emoji, 1)
                }
            }

            row?.addView(
                button,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }
    }

    private fun deletePreviousCharacter() {
        val inputConnection: InputConnection = currentInputConnection ?: return
        inputConnection.deleteSurroundingText(1, 0)
    }
}
