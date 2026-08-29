package com.greyhoundshop073.myemojikeyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MyEmojiInputMethodService : InputMethodService() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var suggestions: LinearLayout

    private var mode = KeyboardMode.LETTERS
    private var shiftOn = false
    private var capsLock = false
    private var symbolsPage = false

    private enum class KeyboardMode {
        LETTERS,
        SYMBOLS,
        EMOJI,
        SAVED,
        CLIPBOARD
    }

    private val recentEmojis = mutableListOf<String>()

    private val letterRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    private val symbolRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")"),
        listOf(".", ",", "?", "!", "'", "\"", ":", ";", "/")
    )

    private val symbolRowsTwo = listOf(
        listOf("[", "]", "{", "}", "<", ">", "=", "%", "^"),
        listOf("*", "~", "`", "|", "\\", "€", "£", "¥"),
        listOf("©", "®", "™", "§", "°", "±", "×", "÷")
    )

    private val emojiCategories = linkedMapOf(

        "😀" to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
            "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
            "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜",
            "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏",
            "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
            "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠",
            "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨",
            "😰", "😥", "😓", "🤗", "🤔", "🤭", "🫢", "🤫",
            "😶", "🫠", "😐", "😑", "😬", "🙄", "😯", "😮",
            "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🤢",
            "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈",
            "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "☠️",
            "👽", "👾", "🤖", "🎃"
        ),

        "❤️" to listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🩷",
            "🩵", "🩶", "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹",
            "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
            "💟", "💌", "💋", "💯", "💢", "💥", "💫", "💦",
            "💨", "💤", "💮", "💯", "💬", "💭", "💎", "💍"
        ),

        "👋" to listOf(
            "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤏", "✌️",
            "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇",
            "☝️", "👍", "👎", "✊", "👊", "🤲", "👏", "🙌",
            "👐", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🫶",
            "👶", "🧒", "👦", "👧", "🧑", "👨", "👩", "🧔",
            "👵", "👴", "🙍", "🙎", "🙅", "🙆", "💁", "🙋",
            "🧏", "🙇", "🤦", "🤷", "💆", "💇", "🚶", "🏃"
        ),

        "🐶" to listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
            "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🙈",
            "🙉", "🙊", "🐔", "🐧", "🐦", "🐤", "🐣", "🦆",
            "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝",
            "🐛", "🦋", "🐌", "🐞", "🐜", "🪲", "🕷️", "🦂",
            "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦀",
            "🐠", "🐟", "🐡", "🦈", "🐬", "🐳", "🐋", "🦭",
            "🐊", "🐘", "🦏", "🦛", "🦒", "🦘", "🦬", "🐪"
        ),

        "🍔" to listOf(
            "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓",
            "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝",
            "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️", "🫑",
            "🌽", "🥕", "🧄", "🧅", "🥔", "🍞", "🥐", "🥖",
            "🧀", "🥚", "🍳", "🧈", "🥞", "🧇", "🥓", "🥩",
            "🍗", "🍔", "🍟", "🍕", "🌭", "🌮", "🌯", "🥗",
            "🍿", "🍩", "🍪", "🎂", "🍰", "🧁", "🍫", "🍭",
            "☕", "🧃", "🥤", "🧋", "🍵", "🍺", "🍷"
        ),

        "⚽" to listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉",
            "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍",
            "🏏", "⛳", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽",
            "🛹", "🛼", "🛷", "⛸️", "🥌", "🎿", "⛷️", "🏂",
            "🏋️", "🤼", "🤸", "⛹️", "🤺", "🏇", "🏄", "🏊"
        ),

        "🚗" to listOf(
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑",
            "🚒", "🚐", "🛻", "🚚", "🚛", "🚜", "🛵", "🏍️",
            "🚲", "🛴", "✈️", "🚀", "🛸", "🚁", "🚢", "⛵",
            "🚤", "🚂", "🚆", "🚇", "🚉", "🚊", "🚝", "🚞",
            "🛶", "🛟", "🗺️", "🗿", "🗽", "🗼", "🏰", "🏠"
        ),

        "🌸" to listOf(
            "🌸", "🌹", "🌺", "🌻", "🌼", "🌷", "🪻", "🌱",
            "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀",
            "🍁", "🍂", "🍃", "🌊", "🔥", "⭐", "🌟", "✨",
            "☀️", "🌤️", "⛅", "🌧️", "⛈️", "🌩️", "❄️", "☃️",
            "🌈", "🌙", "🌎", "🌍", "🌏", "🌑", "🌕", "🌞"
        ),

        "✨" to listOf(
            "⭐", "🌟", "✨", "💫", "🔥", "💎", "👑", "🎯",
            "✅", "❌", "❗", "❓", "‼️", "⁉️", "⚠️", "⭕",
            "➕", "➖", "✖️", "➗", "♾️", "💯", "©️", "®️",
            "™️", "✔️", "☑️", "🔴", "🟠", "🟡", "🟢", "🔵",
            "🟣", "⚫", "⚪", "🟤", "🔷", "🔶", "🔺", "🔻",
            "🔰", "♻️", "⚡", "☀️", "☁️", "☂️", "☮️",
            "☯️", "✝️", "☪️", "🕉️", "☸️", "✡️", "🔱", "⚜️"
        ),

        "🎉" to listOf(
            "🎉", "🎊", "🎈", "🎁", "🎂", "🎄", "🎃", "🎗️",
            "🎟️", "🎫", "🏆", "🏅", "🥇", "🥈", "🥉", "🎖️",
            "🎵", "🎶", "🎤", "🎧", "🎸", "🎹", "🥁", "🎺",
            "🎻", "🎬", "🎨", "🎭", "🎮", "🕹️", "🎲", "🧩"
        )
    )

    override fun onCreateInputView(): View {
        buildKeyboard()
        return root
    }

    override fun onStartInput(
        attribute: EditorInfo,
        restarting: Boolean
    ) {
        super.onStartInput(attribute, restarting)

        mode = KeyboardMode.LETTERS
        symbolsPage = false

        val inputClass =
            attribute.inputType and InputType.TYPE_MASK_CLASS

        if (inputClass == InputType.TYPE_CLASS_NUMBER ||
            inputClass == InputType.TYPE_CLASS_DATETIME
        ) {
            mode = KeyboardMode.SYMBOLS
        }

        if (inputClass == InputType.TYPE_CLASS_TEXT) {
            val variation =
                attribute.inputType and InputType.TYPE_MASK_VARIATION

            if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            ) {
                mode = KeyboardMode.LETTERS
            }
        }

        updateCapitalization(attribute)
    }

    private fun updateCapitalization(info: EditorInfo?) {
        if (info == null) return

        val caps =
            info.initialCapsMode and
                    (InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS or
                            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)

        shiftOn = caps != 0
        capsLock = false
    }

    private fun buildKeyboard() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(238, 238, 238))
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        toolbar.addView(
            topButton("🔤") {
                mode = KeyboardMode.LETTERS
                render()
            }
        )

        toolbar.addView(
            topButton("😀") {
                mode = KeyboardMode.EMOJI
                render()
            }
        )

        toolbar.addView(
            topButton("⭐") {
                mode = KeyboardMode.SAVED
                render()
            }
        )

        toolbar.addView(
            topButton("📋") {
                mode = KeyboardMode.CLIPBOARD
                render()
            }
        )

        toolbar.addView(
            topButton("123") {
                mode = KeyboardMode.SYMBOLS
                render()
            }
        )

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        suggestions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        root.addView(
            suggestions,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
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
        suggestions.removeAllViews()

        when (mode) {

            KeyboardMode.LETTERS -> {
                renderSuggestions()
                renderLetters()
            }

            KeyboardMode.SYMBOLS -> {
                renderSymbols()
            }

            KeyboardMode.EMOJI -> {
                renderEmojis()
            }

            KeyboardMode.SAVED -> {
                renderSaved()
            }

            KeyboardMode.CLIPBOARD -> {
                renderClipboard()
            }
        }
    }

    private fun renderSuggestions() {

        val connection = currentInputConnection ?: return

        val before =
            connection.getTextBeforeCursor(40, 0)?.toString() ?: ""

        val currentWord =
            before.takeLastWhile { !it.isWhitespace() }

        val candidates = mutableListOf<String>()

        if (currentWord.isNotEmpty()) {

            val common = listOf(
                "the",
                "this",
                "that",
                "and",
                "you",
                "your",
                "are",
                "with",
                "have",
                "for",
                "from",
                "good",
                "morning",
                "hello",
                "thank",
                "thanks",
                "please",
                "love",
                "today",
                "tomorrow"
            )

            common
                .filter {
                    it.startsWith(
                        currentWord.lowercase()
                    )
                }
                .take(3)
                .forEach {
                    candidates.add(it)
                }
        } else {
            candidates.add("😀")
            candidates.add("❤️")
            candidates.add("✨")
        }

        candidates.forEach { candidate ->

            val button = Button(this).apply {
                text = candidate
                textSize = 13f
                isAllCaps = false

                setOnClickListener {

                    if (currentWord.isNotEmpty()) {
                        connection.deleteSurroundingText(
                            currentWord.length,
                            0
                        )
                    }

                    connection.commitText(
                        candidate,
                        1
                    )
                }
            }

            suggestions.addView(
                button,
                LinearLayout.LayoutParams(
                    0,
                    dp(40),
                    1f
                )
            )
        }
    }

    private fun renderLetters() {

        letterRows.forEachIndexed { rowIndex, row ->

            val rowView = keyboardRow()

            if (rowIndex == 1) {
                rowView.setPadding(
                    dp(12),
                    dp(2),
                    dp(12),
                    dp(2)
                )
            }

            row.forEach { letter ->

                val display =
                    if (shiftOn || capsLock) {
                        letter.uppercase()
                    } else {
                        letter
                    }

                rowView.addView(
                    keyButton(display) {
                        commitText(display)

                        if (shiftOn && !capsLock) {
                            shiftOn = false
                            render()
                        } else {
                            renderSuggestions()
                        }
                    },
                    keyParams()
                )
            }

            content.addView(rowView)
        }

        val bottom = keyboardRow()

        bottom.addView(
            keyButton(
                if (capsLock) "⇧" else "↑"
            ) {
                if (shiftOn && !capsLock) {
                    capsLock = true
                    shiftOn = true
                } else if (capsLock) {
                    capsLock = false
                    shiftOn = false
                } else {
                    shiftOn = true
                }

                render()
            },
            keyParams(1.15f)
        )

        bottom.addView(
            keyButton("123") {
                mode = KeyboardMode.SYMBOLS
                render()
            },
            keyParams(1.15f)
        )

        bottom.addView(
            keyButton("😀") {
                mode = KeyboardMode.EMOJI
                render()
            },
            keyParams(1.15f)
        )

        bottom.addView(
            keyButton("SPACE") {
                commitText(" ")
                renderSuggestions()
            },
            keyParams(3.2f)
        )

        bottom.addView(
            keyButton("⌫") {
                deletePreviousCharacter()
                renderSuggestions()
            },
            keyParams(1.15f)
        )

        bottom.addView(
            keyButton(actionKeyLabel()) {
                sendEnter()
            },
            keyParams(1.15f)
        )

        content.addView(bottom)
    }

    private fun renderSymbols() {

        val rows =
            if (symbolsPage) {
                symbolRowsTwo
            } else {
                symbolRows
            }

        rows.forEach { row ->

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
            keyButton(
                if (symbolsPage) "1/2" else "2/2"
            ) {
                symbolsPage = !symbolsPage
                render()
            },
            keyParams(1.25f)
        )

        bottom.addView(
            keyButton("ABC") {
                mode = KeyboardMode.LETTERS
                render()
            },
            keyParams(1.25f)
        )

        bottom.addView(
            keyButton("😀") {
                mode = KeyboardMode.EMOJI
                render()
            },
            keyParams(1.25f)
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

    private fun renderEmojis() {

        val categoryScroll =
            HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
            }

        val categoryRow =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

        categoryRow.addView(
            topButton("🕘") {
                renderRecentEmojis()
            }
        )

        emojiCategories.keys.forEach { category ->

            categoryRow.addView(
                topButton(category) {
                    showEmojiCategory(category)
                }
            )
        }

        categoryScroll.addView(categoryRow)

        content.addView(
            categoryScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        showEmojiCategory(
            emojiCategories.keys.first()
        )
    }

    private fun showEmojiCategory(category: String) {

        if (content.childCount > 0) {
            content.removeViews(
                1,
                content.childCount - 1
            )
        }

        createItemGrid(
            emojiCategories[category] ?: emptyList()
        )

        addEmojiBottomRow()
    }

    private fun renderRecentEmojis() {

        if (content.childCount > 0) {
            content.removeViews(
                1,
                content.childCount - 1
            )
        }

        if (recentEmojis.isEmpty()) {

            val message = TextView(this).apply {
                text = "🕘 No recently used emojis yet."
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(
                    dp(20),
                    dp(40),
                    dp(20),
                    dp(40)
                )
            }

            content.addView(message)

        } else {
            createItemGrid(recentEmojis)
        }

        addEmojiBottomRow()
    }

    private fun addEmojiBottomRow() {

        val bottom = keyboardRow()

        bottom.addView(
            keyButton("🔤") {
                mode = KeyboardMode.LETTERS
                render()
            },
            keyParams(1.15f)
        )

        bottom.addView(
            keyButton("⭐") {
                mode = KeyboardMode.SAVED
                render()
            },
            keyParams(1.15f)
        )

        bottom.addView(
            keyButton("📋") {
                mode = KeyboardMode.CLIPBOARD
                render()
            },
            keyParams(1.15f)
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
            keyParams(1.15f)
        )

        content.addView(bottom)
    }

    private fun renderSaved() {

        val saved =
            SavedItemStore.getSavedItems(this)

        if (saved.isEmpty()) {

            val message = TextView(this).apply {
                text =
                    "⭐ Your saved items will appear here.\n\n" +
                            "Long-press an emoji, symbol, or clipboard item to save it."
                textSize = 17f
                gravity = Gravity.CENTER
                setPadding(
                    dp(20),
                    dp(50),
                    dp(20),
  
