package com.greyhoundshop073.myemojikeyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.SystemClock
import android.view.Gravity
import android.view.HapticFeedbackConstants
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

// 1. Persistence Helper for Saved Items & Recent Emojis (Fixes the missing class)
object SavedItemStore {
    private const val PREFS_NAME = "emoji_keyboard_prefs"
    private const val KEY_SAVED = "saved_items"
    private const val KEY_RECENT = "recent_emojis"
    private const val DELIMITER = ","

    fun getSavedItems(context: Context): List<String> = getStringList(context, KEY_SAVED)

    fun saveItem(context: Context, item: String) {
        val items = getSavedItems(context).toMutableList()
        if (item !in items) {
            items.add(item)
            saveStringList(context, KEY_SAVED, items)
        }
    }

    fun getRecentEmojis(context: Context): List<String> = getStringList(context, KEY_RECENT)

    fun addRecentEmoji(context: Context, item: String) {
        val items = getRecentEmojis(context).toMutableList()
        items.remove(item)
        items.add(0, item)
        saveStringList(context, KEY_RECENT, items.take(30))
    }

    private fun getStringList(context: Context, key: String): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val combined = prefs.getString(key, "") ?: ""
        return if (combined.isEmpty()) emptyList() else combined.split(DELIMITER)
    }

    private fun saveStringList(context: Context, key: String, list: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, list.joinToString(DELIMITER)).apply()
    }
}

class MyEmojiInputMethodService : InputMethodService() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var toolbar: LinearLayout
    private var emojiSearchBar: EditText? = null

    private var mode = Mode.LETTERS
    private var shiftOn = false
    private var capsLock = false
    private var symbolsPage = false
    private var lastSpaceTapTime = 0L
    private var audioManager: AudioManager? = null

    private enum class Mode { LETTERS, EMOJI, SYMBOLS, SAVED }

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

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onCreateInputView(): View {
        buildKeyboard()
        return root
    }

    // Modern UI Helpers
    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun createKeyBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(8).toFloat()
            setColor(color)
            setStroke(1, Color.parseColor("#D0D0D0"))
        }
    }

    private fun performKeyAction(action: () -> Unit) {
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        action()
    }

    private fun buildKeyboard() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(230, 230, 230))
            setPadding(2, 2, 2, 2)
        }

        // Toolbar
        toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(2, 2, 2, 2)
        }

        toolbar.addView(createToolbarButton("🔤") { mode = Mode.LETTERS; render() })
        toolbar.addView(createToolbarButton("😀") { mode = Mode.EMOJI; render() })
        toolbar.addView(createToolbarButton("⭐") { mode = Mode.SAVED; render() })
        toolbar.addView(createToolbarButton("123") { mode = Mode.SYMBOLS; render() })
        toolbar.addView(createToolbarButton("📋") { openClipboard() })

        root.addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Content Area
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        render()
    }

    private fun createToolbarButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            isAllCaps = false
            setPadding(8, 4, 8, 4)
            background = createKeyBackground(Color.WHITE)
            setOnClickListener {
                performKeyAction(action)
            }
        }.also {
            it.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dpToPx(2), dpToPx(1), dpToPx(2), dpToPx(1))
            }
        }
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
            if (rowIndex == 1) rowView.setPadding(dpToPx(20), dpToPx(2), dpToPx(20), dpToPx(2))

            row.forEach { letter ->
                val display = if (shiftOn || capsLock) letter.uppercase() else letter
                rowView.addView(createKey(display, action = { commitLetter(display) }))
            }
            content.addView(rowView)
        }

        val bottom = keyboardRow()
        bottom.addView(createKey(if (capsLock) "⇪" else if (shiftOn) "⇧" else "↑", weight = 1.2f, action = {
            if (shiftOn) { shiftOn = false; capsLock = true } else { shiftOn = true; capsLock = false }
            render()
        }))
        bottom.addView(createKey("🌐", weight = 1f, action = { mode = Mode.EMOJI; render() }))
        bottom.addView(createKey("SPACE", weight = 3.4f, action = { handleSpace() }))
        bottom.addView(createKey("⌫", weight = 1.2f, action = { deletePreviousChar() }, longPress = { deletePreviousChar() }))
        bottom.addView(createKey("↵", weight = 1.2f, action = { sendEnter() }))
        content.addView(bottom)
    }

    private fun renderSymbols() {
        val data = if (symbolsPage) symbolPageTwo else symbols
        data.forEach { row ->
            val rowView = keyboardRow()
            row.forEach { symbol ->
                rowView.addView(createKey(symbol, action = { commitText(symbol) },
                    longPress = {
                        // Add extra symbols on long press
                        if (symbol == ".") commitText("...", 1)
                        else commitText(symbol, 1)
                    }))
            }
            content.addView(rowView)
        }

        val bottom = keyboardRow()
        bottom.addView(createKey(if (symbolsPage) "1/2" else "2/2", weight = 1.4f, action = { symbolsPage = !symbolsPage; render() }))
        bottom.addView(createKey("ABC", weight = 1.3f, action = { mode = Mode.LETTERS; render() }))
        bottom.addView(createKey("SPACE", weight = 3f, action = { handleSpace() }))
        bottom.addView(createKey("⌫", weight = 1.2f, action = { deletePreviousChar() }, longPress = { deletePreviousChar() }))
        bottom.addView(createKey("↵", weight = 1.2f, action = { sendEnter() }))
        content.addView(bottom)
    }

    private fun renderEmojis() {
        // Search Bar
        emojiSearchBar = EditText(this).apply {
            hint = "Search Emojis..."
            textSize = 16f
            setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5))
            setBackgroundColor(Color.WHITE)
        }
        content.addView(emojiSearchBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
        })
        
        // Categories
        val categoryBar = HorizontalScrollView(this)
        val categories = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        
        // Add Recent as first category
        categories.addView(createCategoryButton("🕘") { showRecentEmojis() })
        
        emojiCategories.keys.forEach { category ->
            categories.addView(createCategoryButton(category) { showEmojiCategory(category) })
        }
        categoryBar.addView(categories)
        content.addView(categoryBar)

        showEmojiCategory(emojiCategories.keys.first())
    }

    private fun createCategoryButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 18f
            isAllCaps = false
            background = createKeyBackground(Color.TRANSPARENT)
            setOnClickListener { performKeyAction(action) }
        }
    }

    private fun showRecentEmojis() {
        val searchText = emojiSearchBar?.text?.toString() ?: ""
        val recent = SavedItemStore.getRecentEmojis(this)
        val toShow = if (searchText.isNotEmpty()) recent.filter { it.contains(searchText) } else recent
        displayEmojiGrid(toShow)
        addBottomNavBar()
    }

    private fun showEmojiCategory(category: String) {
        val searchText = emojiSearchBar?.text?.toString() ?: ""
        val baseList = emojiCategories[category] ?: emptyList()
        val toShow = if (searchText.isNotEmpty()) baseList.filter { it.contains(searchText) } else baseList
        displayEmojiGrid(toShow)
        addBottomNavBar()
    }

    private fun displayEmojiGrid(items: List<String>) {
        val scrollView = ScrollView(this)
        val gridContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        var row: LinearLayout? = null

        items.forEachIndexed { index, item ->
            if (index % 8 == 0) {
                row = keyboardRow()
                gridContainer.addView(row)
            }

            val button = TextView(this).apply {
                text = item
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(4, 10, 4, 10)
                background = createKeyBackground(Color.TRANSPARENT)
                setOnClickListener {
                    performKeyAction {
                        commitText(item)
                        SavedItemStore.addRecentEmoji(this@MyEmojiInputMethodService, item)
                    }
                }
            }
            row?.addView(button, LinearLayout.LayoutParams(0, dpToPx(50), 1f).apply { setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)) })
        }
        
        scrollView.addView(gridContainer)
        content.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    }

    private fun addBottomNavBar() {
        val bottom = keyboardRow()
        bottom.addView(createKey("🔤", weight = 1.2f, action = { mode = Mode.LETTERS; render() }))
        bottom.addView(createKey("⭐", weight = 1.2f, action = { mode = Mode.SAVED; render() }))
        bottom.addView(createKey("SPACE", weight = 3f, action = { handleSpace() }))
        bottom.addView(createKey("⌫", weight = 1.2f, action = { deletePreviousChar() }, longPress = { deletePreviousChar() }))
        content.addView(bottom)
    }

    private fun renderSaved() {
        val saved = SavedItemStore.getSavedItems(this)
        if (saved.isEmpty()) {
            val message = TextView(this).apply {
                text = "⭐ Your saved items will appear here.\n\nLong-press an emoji to save it."
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(20, 60, 20, 60)
            }
            content.addView(message)
        } else {
            displayEmojiGrid(saved)
        }
        val bottom = keyboardRow()
        bottom.addView(createKey("🔤", weight = 1.2f, action = { mode = Mode.LETTERS; render() }))
        bottom.addView(createKey("😀", weight = 1.2f, action = { mode = Mode.EMOJI; render() }))
        bottom.addView(createKey("SPACE", weight = 3f, action = { handleSpace() }))
        bottom.addView(createKey("⌫", weight = 1.2f, action = { deletePreviousChar() }, longPress = { deletePreviousChar() }))
        content.addView(bottom)
    }

    private fun keyboardRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
        }
    }

    // High Quality Key with long-press support
    private fun createKey(text: String, weight: Float = 1f, action: () -> Unit, longPress: () -> Unit = action): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = if (text.length > 4) 14f else 20f
            gravity = Gravity.CENTER
            isAllCaps = false
            setTextColor(Color.BLACK)
            background = createKeyBackground(Color.WHITE)
            setOnClickListener { performKeyAction(action) }
            setOnLongClickListener { performKeyAction(longPress); true }
        }.also {
            it.layoutParams = LinearLayout.LayoutParams(0, dpToPx(50), weight).apply {
                setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
            }
        }
    }

    // Advanced Input Handling
    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun commitLetter(letter: String) {
        val conn = currentInputConnection ?: return
        val shouldAutoCap = isStartOfSentence(conn)
        if (shouldAutoCap && letter.isLowerCase()) {
            conn.commitText(letter.uppercase(), 1)
        } else {
            conn.commitText(letter, 1)
        }
    }

    private fun isStartOfSentence(conn: InputConnection): Boolean {
        val beforeCursor = conn.getTextBeforeCursor(2, 0)?.toString() ?: return true
        if (beforeCursor.isEmpty()) return true
        val lastChar = beforeCursor.last()
        return lastChar == '.' || lastChar == '!' || lastChar == '?' || lastChar == '\n'
    }

    // Double tap space for period
    private fun handleSpace() {
        val now = System.currentTimeMillis()
        if (now - lastSpaceTapTime < 400) {
            currentInputConnection?.deleteSurroundingTextInCodePoints(1, 0)
            currentInputConnection?.commitText(". ", 1)
            lastSpaceTapTime = 0
        } else {
            commitText(" ")
            lastSpaceTapTime = now
        }
    }

    private fun deletePreviousChar() {
        val conn = currentInputConnection ?: return
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
        val selected = conn.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            conn.commitText("", 1)
        } else {
            conn.deleteSurroundingTextInCodePoints(1, 0)
        }
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

    // Functional Clipboard Manager
    private fun openClipboard() {
     
