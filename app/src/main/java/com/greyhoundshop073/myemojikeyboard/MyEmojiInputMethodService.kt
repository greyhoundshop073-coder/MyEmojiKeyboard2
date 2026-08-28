package com.greyhoundshop073.myemojikeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MyEmojiInputMethodService : InputMethodService() {

    private lateinit var emojiContainer: LinearLayout

    private val categories = linkedMapOf(
        "😀" to listOf(
            "😀","😃","😄","😁","😆","😅","😂","🤣",
            "😊","😇","🙂","🙃","😉","😌","😍","🥰",
            "😘","😗","😙","😚","😋","😛","😝","😜",
            "🤪","🤨","🧐","🤓","😎","🤩","🥳","😏",
            "😒","😞","😔","😟","😕","🙁","☹️","😣",
            "😖","😫","😩","🥺","😢","😭","😤","😠",
            "😡","🤬","🤯","😳","🥵","🥶","😱","😨",
            "😰","😥","😓","🤗","🤔","🤭","🫢","🤫",
            "😶","🫠","😐","😑","😬","🙄","😯","😮",
            "😲","🥱","😴","🤤"
        ),
        "👋" to listOf(
            "👋","🤚","🖐️","✋","🖖","👌","🤏","✌️",
            "🤞","🤟","🤘","🤙","👈","👉","👆","👇",
            "☝️","👍","👎","✊","👊","🤲","👏","🙌",
            "👐","🤝","🙏","✍️","💅","🤳","💪","🫶",
            "👶","🧒","👦","👧","🧑","👨","👩","🧔",
            "👵","👴","🙍","🙎","🙅","🙆","💁","🙋"
        ),
        "❤️" to listOf(
            "❤️","🧡","💛","💚","💙","💜","🖤","🩷",
            "🩵","🩶","🤍","🤎","💔","❤️‍🔥","❤️‍🩹",
            "❣️","💕","💞","💓","💗","💖","💘","💝",
            "💟","💌","💋","💯","💢","💥","💫","💦"
        ),
        "🐶" to listOf(
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼",
            "🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈",
            "🙉","🙊","🐔","🐧","🐦","🐤","🐣","🦆",
            "🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝",
            "🐛","🦋","🐌","🐞","🐜","🪲","🕷️","🦂",
            "🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦀",
            "🐠","🐟","🐡","🦈","🐬","🐳","🐋","🦭"
        ),
        "🍔" to listOf(
            "🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓",
            "🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝",
            "🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑",
            "🌽","🥕","🧄","🧅","🥔","🍞","🥐","🥖",
            "🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩",
            "🍗","🍔","🍟","🍕","🌭","🌮","🌯","🥗",
            "🍿","🍩","🍪","🎂","🍰","🧁","🍫","🍭",
            "☕","🧃","🥤","🧋","🍵"
        ),
        "✨" to listOf(
            "⭐","🌟","✨","💫","🔥","💎","👑","🎯",
            "✅","❌","❗","❓","‼️","⁉️","⚠️","⭕",
            "➕","➖","✖️","➗","♾️","💯","©️","®️",
            "™️","✔️","☑️","🔴","🟠","🟡","🟢","🔵",
            "🟣","⚫","⚪","🟤","🔷","🔶","🔺","🔻",
            "🔰","♻️","⚡","☀️","☁️","☂️","☮️",
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
            setPadding(6, 6, 6, 6)
        }

        val title = TextView(this).apply {
            text = "😀  My Emoji Keyboard"
            textSize = 18f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(8, 6, 8, 8)
        }

        root.addView(title)

        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val savedButton = Button(this).apply {
            text = "⭐"
            textSize = 18f
            setOnClickListener {
                displaySavedItems()
            }
        }

        categoryRow.addView(savedButton)

        categories.keys.forEach { category ->
            val button = Button(this).apply {
                text = category
                textSize = 18f
                setOnClickListener {
                    displayCategory(category)
                }
            }

            categoryRow.addView(button)
        }

        categoryScroll.addView(categoryRow)
        root.addView(categoryScroll)

        val scrollView = ScrollView(this)

        emojiContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        scrollView.addView(emojiContainer)

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
                sendEnter()
            }
        }

        bottomRow.addView(
            spaceButton,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        bottomRow.addView(
            deleteButton,
            LinearLayout.LayoutParams(0, -2, 0.35f)
        )

        bottomRow.addView(
            enterButton,
            LinearLayout.LayoutParams(0, -2, 0.35f)
        )

        root.addView(bottomRow)

        displayCategory("😀")

        return root
    }

    private fun displayCategory(category: String) {
        emojiContainer.removeAllViews()

        val emojis = categories[category] ?: emptyList()

        createEmojiGrid(emojis)
    }

    private fun displaySavedItems() {
        emojiContainer.removeAllViews()

        val savedItems = SavedItemStore.getSavedItems(this)

        if (savedItems.isEmpty()) {
            val emptyMessage = TextView(this).apply {
                text = "⭐ No saved items yet.\n\nLong-press an emoji to save it here."
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(24, 50, 24, 50)
            }

            emojiContainer.addView(emptyMessage)
            return
        }

        createEmojiGrid(savedItems)
    }

    private fun createEmojiGrid(items: List<String>) {
        var row: LinearLayout? = null

        items.forEachIndexed { index, item ->

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
                text = item
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(8, 10, 8, 10)

                setOnClickListener {
                    currentInputConnection?.commitText(item, 1)
                }

                setOnLongClickListener {
                    SavedItemStore.saveItem(this@MyEmojiInputMethodService, item)

                    Toast.makeText(
                        this@MyEmojiInputMethodService,
                        "⭐ Saved $item",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }

                setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        view.alpha = 0.6f
                    }

                    if (event.action == MotionEvent.ACTION_UP ||
                        event.action == MotionEvent.ACTION_CANCEL
                    ) {
                        view.alpha = 1f
                    }

                    false
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
        val connection: InputConnection = currentInputConnection ?: return
        connection.deleteSurroundingText(1, 0)
    }

    private fun sendEnter() {
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
