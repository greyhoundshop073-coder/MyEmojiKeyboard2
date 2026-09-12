package com.greyhoundshop073.myemojikeyboard

import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.HapticFeedbackConstants
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
    private var capsLock = false
    private var symbolsPage = false
    private enum class Mode { LETTERS, EMOJI, SYMBOLS, SAVED, CLIPBOARD, MY_EMOJI, TRANSLATOR }
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
        "😀" to listOf("😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🤗","🤔","🤭","🫢","🤫","😶","🫠","😐","😑","😬","🙄","😯","😮","😲","🥱","😴","🤤"),
        "👋" to listOf("👋","🤚","🖐️","✋","🖖","👌","🤏","✌️","🤞","🤟","🤘","🤙","👈","👉","👆","👇","☝️","👍","👎","✊","👊","🤲","👏","🙌","👐","🤝","🙏","✍️","💅","🤳","💪","🫶","👶","🧒","👦","👧","🧑","👨","👩","🧔","👵","👴","🙍","🙎","🙅","🙆","💁","🙋"),
        "❤️" to listOf("❤️","🧡","💛","💚","💙","💜","🖤","🩷","🩵","🩶","🤍","🤎","💔","❤️‍🔥","❤️‍🩹","❣️","💕","💞","💓","💗","💖","💘","💝","💟","💌","💋","💯","💢","💥","💫","💦"),
        "🐶" to listOf("🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈","🙉","🙊","🐔","🐧","🐦","🐤","🐣","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🐛","🦋","🐌","🐞","🐜","🪲","🕷️","🦂","🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦀","🐠","🐟","🐡","🦈","🐬","🐳","🐋","🦭"),
        "🍔" to listOf("🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑","🌽","🥕","🧄","🧅","🥔","🍞","🥐","🥖","🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩","🍗","🍔","🍟","🍕","🌭","🌮","🌯","🥗","🍿","🍩","🍪","🎂","🍰","🧁","🍫","🍭","☕","🧃","🥤","🧋","🍵"),
        "⚽" to listOf("⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍","🏏","⛳","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷","⛸️","🥌","🎿","⛷️","🏂"),
        "🚗" to listOf("🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🛵","🏍️","🚲","🛴","✈️","🚀","🛸","🚁","🚢","⛵","🚤","🚂","🚆","🚇","🚉","🚊","🚝","🚞"),
        "✨" to listOf("⭐","🌟","✨","💫","🔥","💎","👑","🎯","✅","❌","❗","❓","‼️","⁉️","⚠️","⭕","➕","➖","✖️","➗","♾️","💯","©️","®️","™️","✔️","☑️","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔷","🔶","🔺","🔻","🔰","♻️","⚡","☀️","☁️","☂️","☮️","☯️","✝️","☪️","🕉️","☸️","✡️","🔱","⚜️")
    )
    private val accent = Color.rgb(0, 188, 255)
    private val accentPurple = Color.rgb(126, 65, 255)
    private val surface = Color.rgb(12, 22, 43)
    private val surfaceLight = Color.rgb(22, 38, 67)
    private val text = Color.WHITE

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        if (restarting) return
        mode = Mode.LETTERS
        shiftOn = false
        capsLock = false
        symbolsPage = false
        if (::root.isInitialized && ::content.isInitialized) render()
    }

    override fun onCreateInputView(): View {
        buildKeyboard()
        return root
    }

    private fun buildKeyboard() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(6), dp(6), dp(6))
            background = gradient(intArrayOf(Color.rgb(5, 12, 29), Color.rgb(25, 12, 50)), 18f)
        }
        val utility = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(3), dp(4), dp(3))
            background = gradient(intArrayOf(Color.rgb(13, 29, 51), Color.rgb(20, 25, 55)), 22f)
        }
        utility.addView(smallButton("ABC") { mode = Mode.LETTERS; render() }, utilityButtonParams())
        utility.addView(smallButton("123") { mode = Mode.SYMBOLS; symbolsPage = false; render() }, utilityButtonParams())
        utility.addView(smallButton("☺") { mode = Mode.EMOJI; render() }, utilityButtonParams())
        utility.addView(smallButton("GIF") { toast("GIFs are coming next") }, utilityButtonParams())
        utility.addView(smallButton("▣") { toast("Sticker studio is coming next") }, utilityButtonParams())
        utility.addView(smallButton("📋") { mode = Mode.CLIPBOARD; render() }, utilityButtonParams())
        utility.addView(smallButton("★") { mode = Mode.SAVED; render() }, utilityButtonParams())
        utility.addView(smallButton("✦") { mode = Mode.MY_EMOJI; render() }, utilityButtonParams())
        utility.addView(smallButton("🌐") { mode = Mode.TRANSLATOR; render() }, utilityButtonParams())
        val utilityScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(utility, ViewGroup.LayoutParams(dp(9 * 68), -1))
        }
        root.addView(utilityScroll, LinearLayout.LayoutParams(-1, dp(48)))
        val suggestion = TextView(this).apply {
            text = "✨  My Emoji  •  Express yourself  •  Your vibe, your keyboard"
            textSize = 12f
            setTextColor(Color.rgb(190, 220, 255))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }
        root.addView(suggestion, LinearLayout.LayoutParams(-1, dp(32)))
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        render()
    }

    private fun render() {
        content.removeAllViews()
        when (mode) {
            Mode.LETTERS -> renderLetters()
            Mode.EMOJI -> renderEmojis()
            Mode.SYMBOLS -> renderSymbols()
            Mode.SAVED -> renderSaved()
            Mode.CLIPBOARD -> renderClipboard()
            Mode.MY_EMOJI -> renderMyEmoji()
            Mode.TRANSLATOR -> renderTranslator()
        }
    }

    private fun renderTranslator() {
        addSectionTitle("TRANSLATOR", "🌐  Translate and insert into the current app")
        val initialText = currentInputConnection?.getTextBeforeCursor(500, 0)?.toString().orEmpty()
        val panel = TranslatorPanel(context = this, initialText = initialText, onInsertTranslation = { translated ->
            commitText(translated)
            mode = Mode.LETTERS
            render()
        })
        content.addView(panel.createView(), LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.2f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.2f))
        bottom.addView(keyButton("MY") { mode = Mode.MY_EMOJI; render() }, keyParams(1.2f))
        bottom.addView(keyButton("★") { mode = Mode.SAVED; render() }, keyParams(1.2f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.2f))
        content.addView(bottom)
    }

    private fun renderLetters() {
        letters.forEachIndexed { rowIndex, row ->
            val rowView = keyboardRow()
            if (rowIndex == 1) rowView.setPadding(dp(18), dp(2), dp(18), dp(2))
            row.forEach { letter ->
                val display = if (shiftOn || capsLock) letter.uppercase() else letter
                rowView.addView(keyButton(display) {
                    commitText(display)
                    if (shiftOn && !capsLock) { shiftOn = false; render() }
                }, keyParams())
            }
            content.addView(rowView)
        }
        val bottom = keyboardRow()
        val shiftButton = keyButton(if (capsLock) "⇧" else if (shiftOn) "⇧" else "↑") {
            shiftOn = !shiftOn
            render()
        }
        shiftButton.setOnLongClickListener {
            shiftButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            capsLock = !capsLock
            shiftOn = false
            render()
            true
        }
        bottom.addView(shiftButton, keyParams(1.15f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1f))
        bottom.addView(keyButton("SPACE") { commitText(" ") }, keyParams(3.3f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.15f))
        bottom.addView(keyButton("↵") { sendEnter() }, keyParams(1.15f))
        content.addView(bottom)
    }

    private fun renderSymbols() {
        val data = if (symbolsPage) symbolPageTwo else symbols
        data.forEach { row ->
            val rowView = keyboardRow()
            row.forEach { symbol -> rowView.addView(keyButton(symbol) { commitText(symbol) }, keyParams()) }
            content.addView(rowView)
        }
        val bottom = keyboardRow()
        bottom.addView(keyButton(if (symbolsPage) "1/2" else "2/2") { symbolsPage = !symbolsPage; render() }, keyParams(1.3f))
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.2f))
        bottom.addView(keyButton("SPACE") { commitText(" ") }, keyParams(3f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.15f))
        bottom.addView(keyButton("↵") { sendEnter() }, keyParams(1.15f))
        content.addView(bottom)
    }

    private fun renderEmojis() {
        val categoryBar = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val categories = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        emojiCategories.keys.forEach { category ->
            categories.addView(smallButton(category) { renderEmojiCategory(category) }, emojiCategoryParams())
        }
        categoryBar.addView(categories)
        content.addView(categoryBar, LinearLayout.LayoutParams(-1, dp(50)))
        renderEmojiCategory(emojiCategories.keys.first())
    }

    private fun renderEmojiCategory(category: String) {
        if (content.childCount > 1) content.removeViews(1, content.childCount - 1)
        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        emojiCategories[category].orEmpty().chunked(8).forEach { row ->
            val rowView = keyboardRow()
            row.forEach { emoji ->
                val button = keyButton(emoji) { commitText(emoji) }
                button.setOnLongClickListener {
                    SavedItemStore.add(this, emoji)
                    toast("Saved $emoji")
                    true
                }
                rowView.addView(button, keyParams())
            }
            repeat(8 - row.size) { rowView.addView(TextView(this), keyParams()) }
            grid.addView(rowView)
        }
        content.addView(grid)
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.2f))
        bottom.addView(keyButton("123") { mode = Mode.SYMBOLS; symbolsPage = false; render() }, keyParams(1.2f))
        bottom.addView(keyButton("★") { mode = Mode.SAVED; render() }, keyParams(1.2f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.2f))
        content.addView(bottom)
    }

    private fun renderSaved() {
        addSectionTitle("SAVED", "★  Your saved emoji and text")
        val items = SavedItemStore.getAll(this)
        if (items.isEmpty()) {
            val empty = TextView(this).apply { text = "No saved items yet. Long-press an emoji to save it."; textSize = 16f; setPadding(dp(16), dp(24), dp(16), dp(24)); gravity = Gravity.CENTER }
            content.addView(empty)
        } else {
            items.chunked(5).forEach { row ->
                val rowView = keyboardRow()
                row.forEach { item ->
                    val button = keyButton(item) { commitText(item) }
                    button.setOnLongClickListener { SavedItemStore.remove(this, item); render(); true }
                    rowView.addView(button, keyParams())
                }
                repeat(5 - row.size) { rowView.addView(TextView(this), keyParams()) }
                content.addView(rowView)
            }
        }
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.2f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.2f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.2f))
        content.addView(bottom)
    }

    private fun renderClipboard() {
        addSectionTitle("CLIPBOARD", "📋  Recent copied text")
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val items = ClipboardStore.getAll(this)
        if (clipboard.hasPrimaryClip()) clipboard.primaryClip?.let { clip -> if (clip.itemCount > 0) ClipboardStore.add(this, clip.getItemAt(0).coerceToText(this).toString()) }
        val currentItems = ClipboardStore.getAll(this)
        if (currentItems.isEmpty()) {
            val empty = TextView(this).apply { text = "No clipboard items yet."; textSize = 16f; setPadding(dp(16), dp(24), dp(16), dp(24)); gravity = Gravity.CENTER }
            content.addView(empty)
        } else {
            currentItems.forEach { item ->
                val button = keyButton(item.take(120)) { commitText(item) }
                content.addView(button, LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(dp(4), dp(3), dp(4), dp(3)) })
            }
        }
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.2f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.2f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.2f))
        content.addView(bottom)
    }

    private fun renderMyEmoji() {
        addSectionTitle("MY EMOJI", "✦  Create and use your personal emoji")
        val panel = MyEmojiCreatorPanel(this) { value -> commitText(value) }
        content.addView(panel.createView(), LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        val items = MyEmojiCreatorStore.getAll(this)
        if (items.isNotEmpty()) {
            items.chunked(5).forEach { row ->
                val rowView = keyboardRow()
                row.forEach { item ->
                    val button = keyButton(item) { commitText(item) }
                    button.setOnLongClickListener { MyEmojiCreatorStore.remove(this, item); render(); true }
                    rowView.addView(button, keyParams())
                }
                repeat(5 - row.size) { rowView.addView(TextView(this), keyParams()) }
                content.addView(rowView)
            }
        }
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.2f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.2f))
        bottom.addView(keyButton("★") { mode = Mode.SAVED; render() }, keyParams(1.2f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.2f))
        content.addView(bottom)
    }

    private fun addSectionTitle(title: String, subtitle: String) {
        val titleView = TextView(this).apply { text = title; textSize = 18f; setTextColor(accent); gravity = Gravity.CENTER; setPadding(dp(8), dp(10), dp(8), dp(2)) }
        val subtitleView = TextView(this).apply { text = subtitle; textSize = 13f; setTextColor(Color.rgb(190, 220, 255)); gravity = Gravity.CENTER; setPadding(dp(8), 0, dp(8), dp(8)) }
        content.addView(titleView)
        content.addView(subtitleView)
    }

    private fun commitText(value: String) { currentInputConnection?.commitText(value, 1) }

    private fun deletePreviousCharacter() {
        val connection = currentInputConnection ?: return
        val selected = connection.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) { connection.commitText("", 1); return }
        connection.deleteSurroundingTextInCodePoints(1, 0)
    }

    private fun sendEnter() {
        val connection = currentInputConnection ?: return
        val options = currentInputEditorInfo
        val action = options?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) connection.performEditorAction(action) else connection.commitText("\n", 1)
        shiftOn = false
    }

    private fun keyboardRow() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(2), dp(2), dp(2), dp(2)) }

    private fun keyButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = if (label.length > 4) 12f else 17f
        isAllCaps = false
        setTextColor(textColor())
        background = gradient(intArrayOf(surfaceLight, Color.rgb(30, 48, 82)), 12f)
        setOnClickListener { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); action() }
    }

    private fun smallButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(textColor())
        background = gradient(intArrayOf(Color.rgb(25, 45, 78), Color.rgb(35, 30, 72)), 12f)
        setOnClickListener { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); action() }
    }

    private fun keyParams(weight: Float = 1f) = LinearLayout.LayoutParams(0, dp(50), weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
    private fun utilityButtonParams() = LinearLayout.LayoutParams(dp(64), dp(42)).apply { setMargins(dp(2), 0, dp(2), 0) }
    private fun emojiCategoryParams() = LinearLayout.LayoutParams(dp(48), dp(46)).apply { setMargins(dp(2), 0, dp(2), 0) }
    private fun textColor() = text
    private fun gradient(colors: IntArray, radius: Float): GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply { cornerRadius = dp(radius.toInt()).toFloat() }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
