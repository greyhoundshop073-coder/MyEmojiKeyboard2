package com.greyhoundshop073.myemojikeyboard

import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
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
        symbolsPage = false

        if (::root.isInitialized && ::content.isInitialized) {
            render()
        }
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
        utility.addView(smallButton("ABC") { mode = Mode.LETTERS; render() }, weightParams(1f))
        utility.addView(smallButton("☺") { mode = Mode.EMOJI; render() }, weightParams(1f))
        utility.addView(smallButton("GIF") { toast("GIFs are coming next") }, weightParams(1f))
        utility.addView(smallButton("▣") { toast("Sticker studio is coming next") }, weightParams(1f))
        utility.addView(smallButton("📋") { mode = Mode.CLIPBOARD; render() }, weightParams(1f))
        utility.addView(smallButton("★") { mode = Mode.SAVED; render() }, weightParams(1f))
        utility.addView(smallButton("✦") { mode = Mode.MY_EMOJI; render() }, weightParams(1f))
        utility.addView(smallButton("🌐") { mode = Mode.TRANSLATOR; render() }, weightParams(1f))
        root.addView(utility, LinearLayout.LayoutParams(-1, dp(48)))

        val suggestion = TextView(this).apply {
            text = "✨  My Emoji  •  Express yourself  •  Your vibe, your keyboard"
            textSize = 12f
            setTextColor(Color.rgb(190, 220, 255))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }
        root.addView(suggestion, LinearLayout.LayoutParams(-1, dp(32)))

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
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
        val panel = TranslatorPanel(
            context = this,
            initialText = initialText,
            onInsertTranslation = { translated ->
                commitText(translated)
                mode = Mode.LETTERS
                render()
            }
        )
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
                val display = if (shiftOn) letter.uppercase() else letter
                rowView.addView(keyButton(display) {
                    commitText(display)
                    if (shiftOn) { shiftOn = false; render() }
                }, keyParams())
            }
            content.addView(rowView)
        }
        val bottom = keyboardRow()
        bottom.addView(keyButton(if (shiftOn) "⇧" else "↑") { shiftOn = !shiftOn; render() }, keyParams(1.15f))
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
        emojiCategories.keys.forEach { category -> categories.addView(smallButton(category) { showEmojiCategory(category) }, LinearLayout.LayoutParams(dp(48), dp(42))) }
        categoryBar.addView(categories)
        content.addView(categoryBar, LinearLayout.LayoutParams(-1, dp(46)))
        showEmojiCategory(emojiCategories.keys.first())
    }

    private fun showEmojiCategory(category: String) {
        val existingCategoryBar = content.getChildAt(0)
        content.removeAllViews()
        if (existingCategoryBar != null) content.addView(existingCategoryBar)
        createItemGrid(emojiCategories[category] ?: emptyList())
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.1f))
        bottom.addView(keyButton("★") { mode = Mode.SAVED; render() }, keyParams(1.1f))
        bottom.addView(keyButton("MY") { mode = Mode.MY_EMOJI; render() }, keyParams(1.1f))
        bottom.addView(keyButton("🌐") { mode = Mode.TRANSLATOR; render() }, keyParams(1.1f))
        bottom.addView(keyButton("SPACE") { commitText(" ") }, keyParams(2.1f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.1f))
        content.addView(bottom)
    }

    private fun renderSaved() {
        val saved = SavedItemStore.getSavedItems(this)
        addSectionTitle("SAVED ITEMS", "★  Your favorites, ready in one tap")
        if (saved.isEmpty()) {
            addEmptyState("★", "Your saved items will appear here", "Long-press an emoji or symbol to save it.")
        } else createItemGrid(saved, allowRemoval = true)
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.1f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.1f))
        bottom.addView(keyButton("MY") { mode = Mode.MY_EMOJI; render() }, keyParams(1.1f))
        bottom.addView(keyButton("🌐") { mode = Mode.TRANSLATOR; render() }, keyParams(1.1f))
        bottom.addView(keyButton("SPACE") { commitText(" ") }, keyParams(2.1f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.1f))
        content.addView(bottom)
    }

    private fun renderMyEmoji() {
        addSectionTitle("MY EMOJI", "✦  Create, save and reuse your personal emoji combinations")

        val panel = MyEmojiCreatorPanel(
            context = this,
            onInsert = { emoji -> commitText(emoji) },
            onClose = { render() },
            onSaved = { render() }
        )
        content.addView(panel.createView(), LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(4), dp(4), dp(4), dp(8))
        })

        val saved = MyEmojiCreatorStore.getCreations(this)
        addSectionTitle("MY COLLECTION", "Tap to insert • Long-press to remove")
        if (saved.isEmpty()) {
            addEmptyState("✦", "No custom emoji saved yet", "Use SAVE in the creator above to keep your favorite combinations here.")
        } else {
            var row: LinearLayout? = null
            saved.forEachIndexed { index, emoji ->
                if (index % 4 == 0) {
                    row = keyboardRow()
                    content.addView(row)
                }
                val item = TextView(this).apply {
                    text = emoji
                    textSize = 27f
                    gravity = Gravity.CENTER
                    setPadding(dp(2), dp(8), dp(2), dp(8))
                    setTextColor(text)
                    background = gradient(intArrayOf(Color.rgb(20, 36, 63), Color.rgb(15, 27, 49)), 12f)
                    setOnClickListener { commitText(emoji) }
                    setOnLongClickListener {
                        MyEmojiCreatorStore.remove(this@MyEmojiInputMethodService, emoji)
                        Toast.makeText(this@MyEmojiInputMethodService, "Removed ✦ $emoji", Toast.LENGTH_SHORT).show()
                        render()
                        true
                    }
                }
                row?.addView(item, LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                })
            }
        }

        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.1f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.1f))
        bottom.addView(keyButton("★") { mode = Mode.SAVED; render() }, keyParams(1.1f))
        bottom.addView(keyButton("🌐") { mode = Mode.TRANSLATOR; render() }, keyParams(1.1f))
        bottom.addView(keyButton("SPACE") { commitText(" ") }, keyParams(2.1f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.1f))
        content.addView(bottom)
    }

    private fun renderClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        val items = mutableListOf<String>()
        if (clip != null) for (index in 0 until clip.itemCount) {
            if (items.size == MAX_CLIPBOARD_ITEMS) break
            val value = clip.getItemAt(index).coerceToText(this).toString().trim()
            if (value.isNotEmpty() && value.length <= MAX_CLIPBOARD_ITEM_LENGTH && !items.contains(value)) {
                items.add(value)
            }
        }
        addSectionTitle("CLIPBOARD", "▣  Quick access to copied text")
        if (items.isEmpty()) addEmptyState("▣", "No text in clipboard", "Copy text from another app, then open Clipboard again.")
        else items.forEach { value ->
            val item = TextView(this).apply {
                text = value
                textSize = 16f
                setTextColor(text)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(12), dp(16), dp(12))
                background = gradient(intArrayOf(surfaceLight, Color.rgb(18, 31, 55)), 14f)
                setOnClickListener { commitText(value) }
            }
            content.addView(item, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(6), dp(4), dp(6), dp(4)) })
        }
        val bottom = keyboardRow()
        bottom.addView(keyButton("ABC") { mode = Mode.LETTERS; render() }, keyParams(1.1f))
        bottom.addView(keyButton("☺") { mode = Mode.EMOJI; render() }, keyParams(1.1f))
        bottom.addView(keyButton("★") { mode = Mode.SAVED; render() }, keyParams(1.1f))
        bottom.addView(keyButton("MY") { mode = Mode.MY_EMOJI; render() }, keyParams(1.1f))
        bottom.addView(keyButton("🌐") { mode = Mode.TRANSLATOR; render() }, keyParams(1.1f))
        bottom.addView(keyButton("SPACE") { commitText(" ") }, keyParams(1.9f))
        bottom.addView(keyButton("⌫") { deletePreviousCharacter() }, keyParams(1.1f))
        content.addView(bottom)
    }

    private fun createItemGrid(items: List<String>, allowRemoval: Boolean = false) {
        var row: LinearLayout? = null
        items.forEachIndexed { index, item ->
            if (index % 8 == 0) { row = keyboardRow(); content.addView(row) }
            val button = TextView(this).apply {
                text = item
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(dp(2), dp(8), dp(2), dp(8))
                setTextColor(text)
                background = gradient(intArrayOf(Color.rgb(20, 36, 63), Color.rgb(15, 27, 49)), 12f)
                setOnClickListener { commitText(item) }
                setOnLongClickListener {
                    if (allowRemoval) {
                        SavedItemStore.removeItem(this@MyEmojiInputMethodService, item)
                        Toast.makeText(this@MyEmojiInputMethodService, "Removed ★ $item", Toast.LENGTH_SHORT).show()
                        render()
                    } else {
                        SavedItemStore.saveItem(this@MyEmojiInputMethodService, item)
                        Toast.makeText(this@MyEmojiInputMethodService, "★ Saved $item", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }
            row?.addView(button, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
        }
    }

    private fun addSectionTitle(title: String, subtitle: String) {
        val block = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(8), dp(12), dp(6)) }
        val heading = TextView(this).apply { text = title; textSize = 17f; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) }
        val sub = TextView(this).apply { text = subtitle; textSize = 11f; setTextColor(Color.rgb(130, 210, 255)) }
        block.addView(heading); block.addView(sub)
        content.addView(block)
    }

    private fun addEmptyState(icon: String, title: String, subtitle: String) {
        val state = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(20), dp(35), dp(20), dp(35)); background = gradient(intArrayOf(surface, Color.rgb(20, 29, 52)), 18f) }
        val i = TextView(this).apply { text = icon; textSize = 34f; gravity = Gravity.CENTER; setTextColor(Color.WHITE) }
        val t = TextView(this).apply { text = title; textSize = 16f; gravity = Gravity.CENTER; setTextColor(Color.WHITE) }
        val s = TextView(this).apply { text = subtitle; textSize = 12f; gravity = Gravity.CENTER; setTextColor(Color.rgb(170, 195, 225)); setPadding(dp(10), dp(8), dp(10), 0) }
        state.addView(i); state.addView(t); state.addView(s)
        content.addView(state, LinearLayout.LayoutParams(-1, dp(150)).apply { setMargins(dp(8), dp(8), dp(8), dp(8)) })
    }

    private fun keyboardRow(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(2), dp(2), dp(2), dp(2)) }

    private fun keyButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = if (label.length > 4) 12f else 17f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = gradient(intArrayOf(Color.rgb(24, 43, 73), Color.rgb(14, 28, 51)), 11f)
        setOnClickListener { action() }
    }

    private fun smallButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = if (label.length > 3) 11f else 17f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = gradient(intArrayOf(Color.rgb(26, 50, 83), Color.rgb(17, 32, 58)), 15f)
        setOnClickListener { action() }
    }

    private fun keyParams(weight: Float = 1f) = LinearLayout.LayoutParams(0, dp(52), weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
    private fun weightParams(weight: Float) = LinearLayout.LayoutParams(0, -1, weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }

    private fun gradient(colors: IntArray, radiusDp: Float): GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply { cornerRadius = dp(radiusDp).toFloat() }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()

    private fun commitText(value: String) { currentInputConnection?.commitText(value, 1) }

    private fun deletePreviousCharacter() {
        val connection: InputConnection = currentInputConnection ?: return
        val selected = connection.getSelectedText(0)
        if (!selected.isNullOrEmpty()) { connection.commitText("", 1); return }
        connection.deleteSurroundingTextInCodePoints(1, 0)
    }

    private fun sendEnter() {
        val editorInfo: EditorInfo? = currentInputEditorInfo
        when (editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
            EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_SEARCH -> sendDefaultEditorAction(true)
            else -> sendKeyChar('\n')
        }
        shiftOn = true
        if (mode == Mode.LETTERS) render()
    }

    private fun toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    private companion object {
        const val MAX_CLIPBOARD_ITEMS = 30
        const val MAX_CLIPBOARD_ITEM_LENGTH = 2000
    }
}
