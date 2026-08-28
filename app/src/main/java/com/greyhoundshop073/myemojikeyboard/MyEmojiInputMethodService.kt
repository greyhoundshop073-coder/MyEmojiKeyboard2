package com.greyhoundshop073.myemojikeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MyEmojiInputMethodService : InputMethodService() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private var mode = Mode.LETTERS
    private var shiftOn = false
    private var symbolsPage = false

    private enum class Mode {
        LETTERS,
        EMOJI,
        SYMBOLS,
        SAVED
    }

    private val letters = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    private val symbols = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")"),
        listOf(".", ",", "?", "!", "'", "\"", ":", ";", "/")
    )

    private val symbolPageTwo = listOf(
        listOf("[", "]", "{", "}", "<", ">", "=", "%", "^"),
        listOf("*", "~", "`", "|", "\\", "€", "£", "¥"),
        listOf("©", "®", "™", "§", "°", "±", "×", "÷")
    )

    private val emojiCategories = linkedMapOf(
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
        "⚽" to listOf(
            "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉",
            "🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍",
            "🏏","⛳","🏹","🎣","🤿","🥊","🥋","🎽",
            "🛹","🛼","🛷","⛸️","🥌","🎿","⛷️","🏂"
        ),
        "🚗" to listOf(
            "🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑",
            "🚒","🚐","🛻","🚚","🚛","🚜","🛵","🏍️",
            "🚲","🛴","✈️","🚀","🛸","🚁","🚢","⛵",
            "🚤","🚂","🚆","🚇","🚉","🚊","🚝","🚞"
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
        buildKeyboard()
        return root
    }

    private fun buildKeyboard() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 245, 245))
            setPadding(4, 4, 4, 4)
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        toolbar.addView(
            smallButton("🔤") {
                mode = Mode.LETTERS
                render()
            }
        )

        toolbar.addView(
            smallButton("😀") {
                mode = Mode.EMOJI
                render()
            }
        )

        toolbar.addView(
            smallButton("⭐") {
                mode = Mode.SAVED
                render()
            }
        )

        toolbar.addView(
            smallButton("123") {
                mode = Mode.SYMBOLS
                render()
            }
        )

        toolbar.addView(
            smallButton("📋") {
                showClipboardInfo()
            }
        )

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            addView(content)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        render()
    }

    private fun render() {
        content.removeAllViews()

        when (mode) {
            Mode.LETTERS -> renderLetters()
            Mode.EMOJI -> renderEmojis()
            Mode.SYMBOLS -> renderSymbols()
            Mode.SAVED -> renderSaved()
        }
    }

    private fun renderLetters() {
        letters.forEachIndexed { rowIndex, row ->
            val rowView = keyboardRow()

            if (rowIndex == 1) {
                rowView.setPadding(20, 2, 20, 2)
            }

            row.forEach { letter ->
                val display = if (shiftOn) letter.uppercase() else letter

                rowView.addView(
                    keyButton(display) {
                        commitText(display)
                    },
                    keyParams()
                )
            }

            content.addView(rowView)
        }

        val bottom = keyboardRow()

        bottom.addView(
            keyButton(if (shiftOn) "⇧" else "↑") {
                shiftOn = !shiftOn
                render()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("🌐") {
                mode = Mode.EMOJI
                render()
            },
            keyParams(1f)
        )

        bottom.addView(
            keyButton("SPACE") {
                commitText(" ")
            },
            keyParams(3.4f)
        )

        bottom.addView(
            keyButton("⌫") {
                deletePreviousCharacter()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("↵") {
                sendEnter()
            },
            keyParams(1.2f)
        )

        content.addView(bottom)
    }

    private fun renderSymbols() {
        val data = if (symbolsPage) symbolPageTwo else symbols

        data.forEach { row ->
            val rowView = keyboardRow()

            row.forEach { symbol ->
                rowView.addView(
                    keyButton(symbol) {
                        commitText(symbol)
                    },
                    keyParams()
                )
            }

            content.addView(rowView)
        }

        val bottom = keyboardRow()

        bottom.addView(
            keyButton(if (symbolsPage) "1/2" else "2/2") {
                symbolsPage = !symbolsPage
                render()
            },
            keyParams(1.4f)
        )

        bottom.addView(
            keyButton("ABC") {
                mode = Mode.LETTERS
                render()
            },
            keyParams(1.3f)
        )

        bottom.addView(
            keyButton("SPACE") {
                commitText(" ")
            },
            keyParams(3f)
        )

        bottom.addView(
            keyButton("⌫") {
                deletePreviousCharacter()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("↵") {
                sendEnter()
            },
            keyParams(1.2f)
        )

        content.addView(bottom)
    }

    private fun renderEmojis() {
        val categoryBar = HorizontalScrollView(this)

        val categories = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        emojiCategories.keys.forEach { category ->
            categories.addView(
                smallButton(category) {
                    showEmojiCategory(category)
                }
            )
        }

        categoryBar.addView(categories)
        content.addView(categoryBar)

        showEmojiCategory(emojiCategories.keys.first())
    }

    private fun showEmojiCategory(category: String) {
        val existingCategoryBar = content.getChildAt(0)

        content.removeAllViews()

        if (existingCategoryBar != null) {
            content.addView(existingCategoryBar)
        }

        createItemGrid(emojiCategories[category] ?: emptyList())

        val bottom = keyboardRow()

        bottom.addView(
            keyButton("🔤") {
                mode = Mode.LETTERS
                render()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("⭐") {
                mode = Mode.SAVED
                render()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("SPACE") {
                commitText(" ")
            },
            keyParams(3f)
        )

        bottom.addView(
            keyButton("⌫") {
                deletePreviousCharacter()
            },
            keyParams(1.2f)
        )

        content.addView(bottom)
    }

    private fun renderSaved() {
        val saved = SavedItemStore.getSavedItems(this)

        if (saved.isEmpty()) {
            val message = TextView(this).apply {
                text = "⭐ Your saved items will appear here.\n\nLong-press an emoji or symbol to save it."
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(20, 60, 20, 60)
            }

            content.addView(message)
        } else {
            createItemGrid(saved)
        }

        val bottom = keyboardRow()

        bottom.addView(
            keyButton("🔤") {
                mode = Mode.LETTERS
                render()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("😀") {
                mode = Mode.EMOJI
                render()
            },
            keyParams(1.2f)
        )

        bottom.addView(
            keyButton("SPACE") {
                commitText(" ")
            },
            keyParams(3f)
        )

        bottom.addView(
            keyButton("⌫") {
                deletePreviousCharacter()
            },
            keyParams(1.2f)
        )

        content.addView(bottom)
    }

    private fun createItemGrid(items: List<String>) {
        var row: LinearLayout? = null

        items.forEachIndexed { index, item ->
            if (index % 8 == 0) {
                row = keyboardRow()
                content.addView(row)
            }

            val button = TextView(this).apply {
                text = item
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(4, 10, 4, 10)

                setBackgroundColor(Color.WHITE)

                setOnClickListener {
                    commitText(item)
                }

                setOnLongClickListener {
                    SavedItemStore.saveItem(
                        this@MyEmojiInputMethodService,
                        item
                    )

                    Toast.makeText(
                        this@MyEmojiInputMethodService,
                        "⭐ Saved $item",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
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

    private fun keyboardRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(2, 2, 2, 2)
        }
    }

    private fun keyButton(
        text: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            this.text = text
            textSize = if (text.length > 4) 13f else 18f
            isAllCaps = false
            setOnClickListener {
                action()
            }
        }
    }

    private fun smallButton(
        text: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            this.text = text
            textSize = 15f
            isAllCaps = false
            setOnClickListener {
                action()
            }
        }
    }

    private fun keyParams(weight: Float = 1f): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            58,
            weight
        ).apply {
            setMargins(2, 2, 2, 2)
        }
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deletePreviousCharacter() {
        val connection: InputConnection = currentInputConnection ?: return

        val selected = connection.getSelectedText(0)

        if (!selected.isNullOrEmpty()) {
            connection.commitText("", 1)
            return
        }

        connection.deleteSurroundingTextInCodePoints(1, 0)
    }

    private fun sendEnter() {
        val connection = currentInputConnection ?: return
        val editorInfo: EditorInfo? = currentInputEditorInfo

        when (editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_SEARCH -> {
                sendDefaultEditorAction(true)
            }

            else -> {
                sendKeyChar('\n')
            }
        }
    }

    private fun showClipboardInfo() {
        Toast.makeText(
            this,
            "Clipboard manager will be added to the next core update.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
