from pathlib import Path
import re

SERVICE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/MyEmojiInputMethodService.kt")
MAIN_ACTIVITY = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/MainActivity.kt")
STORE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/SavedItemStore.kt")
MY_EMOJI_STORE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/MyEmojiCreatorStore.kt")
TRANSLATOR_PANEL = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/TranslatorPanel.kt")
TRANSLATOR_INTEGRATION = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/TranslatorIntegration.kt")
TRANSLATOR_SERVICE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/TranslatorService.kt")
MANIFEST = Path("app/src/main/AndroidManifest.xml")
METHOD_XML = Path("app/src/main/res/xml/method.xml")


def fail(message: str) -> None:
    raise SystemExit(f"VERIFY FAILED: {message}")


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        fail(f"missing {label}: {needle}")


service = SERVICE.read_text(encoding="utf-8")
main_activity = MAIN_ACTIVITY.read_text(encoding="utf-8")
store = STORE.read_text(encoding="utf-8")
my_emoji_store = MY_EMOJI_STORE.read_text(encoding="utf-8")
translator_panel = TRANSLATOR_PANEL.read_text(encoding="utf-8")
translator_integration = TRANSLATOR_INTEGRATION.read_text(encoding="utf-8")
translator_service = TRANSLATOR_SERVICE.read_text(encoding="utf-8")
manifest = MANIFEST.read_text(encoding="utf-8")
method_xml = METHOD_XML.read_text(encoding="utf-8")

# Guard against the duplicate declarations that previously broke compilation.
for declaration in ("private lateinit var root", "private lateinit var content", "private var mode"):
    if len(re.findall(re.escape(declaration), service)) != 1:
        fail(f"expected exactly one declaration of {declaration}")

# Keep every supported keyboard mode wired into rendering.
for mode in ("LETTERS", "EMOJI", "SYMBOLS", "SAVED", "CLIPBOARD", "MY_EMOJI", "TRANSLATOR"):
    require(service, mode, f"Mode.{mode}")

for function in (
    "renderLetters()",
    "renderEmojis()",
    "renderSymbols()",
    "renderSaved()",
    "renderClipboard()",
    "renderMyEmoji()",
    "renderTranslator()",
    "deletePreviousCharacter()",
    "sendEnter()",
):
    require(service, function, function)

# Main utility navigation must expose the existing keyboard modes without
# creating duplicate screens or alternate implementations.
for button in (
    'smallButton("ABC") { mode = Mode.LETTERS; render() }',
    'smallButton("123") { mode = Mode.SYMBOLS; symbolsPage = false; render() }',
    'smallButton("☺") { mode = Mode.EMOJI; render() }',
    'smallButton("📋") { mode = Mode.CLIPBOARD; render() }',
    'smallButton("★") { mode = Mode.SAVED; render() }',
    'smallButton("✦") { mode = Mode.MY_EMOJI; render() }',
    'smallButton("🌐") { mode = Mode.TRANSLATOR; render() }',
):
    require(service, button, "main utility navigation")

# Symbols must expose both pages and a reliable return path to letters.
require(service, 'val data = if (symbolsPage) symbolPageTwo else symbols', "symbols page selection")
require(service, 'keyButton(if (symbolsPage) "1/2" else "2/2") { symbolsPage = !symbolsPage; render() }', "symbols page toggle")
require(service, 'keyButton("ABC") { mode = Mode.LETTERS; render() }', "symbols return to letters")

# New editor sessions must start from a clean transient keyboard state.
require(service, "override fun onStartInput(attribute: EditorInfo?, restarting: Boolean)", "input-session lifecycle hook")
require(service, "if (restarting) return", "restart preservation")
require(service, "mode = Mode.LETTERS", "new-session mode reset")
require(service, "shiftOn = false", "new-session shift reset")
require(service, "symbolsPage = false", "new-session symbols reset")
require(service, "::root.isInitialized", "safe lifecycle view check")
require(service, "::content.isInitialized", "safe lifecycle content check")

# Safety checks for the current editing and persistence behavior.
require(service, "deleteSurroundingTextInCodePoints(1, 0)", "emoji-aware backspace")
require(service, "getSelectedText(0)", "selected-text detection")
require(service, "connection.commitText(\"\", 1)", "selection deletion")
require(service, "sendDefaultEditorAction(true)", "editor-aware enter action")
require(service, "EditorInfo.IME_ACTION_DONE", "DONE editor action")
require(service, "EditorInfo.IME_ACTION_GO", "GO editor action")
require(service, "EditorInfo.IME_ACTION_NEXT", "NEXT editor action")
require(service, "EditorInfo.IME_ACTION_SEND", "SEND editor action")
require(service, "EditorInfo.IME_ACTION_SEARCH", "SEARCH editor action")
require(service, "SavedItemStore.saveItem", "saved-item integration")
require(service, "MyEmojiCreatorStore.getCreations", "My Emoji collection integration")
require(service, "ClipboardManager", "real clipboard integration")

# Clipboard rendering must stay bounded so a large system clipboard cannot
# create an unbounded number of keyboard views or render oversized text.
require(service, "MAX_CLIPBOARD_ITEMS = 30", "clipboard item count limit")
require(service, "MAX_CLIPBOARD_ITEM_LENGTH = 2000", "clipboard item length limit")
require(service, "items.size == MAX_CLIPBOARD_ITEMS", "clipboard item count enforcement")
require(service, "value.length <= MAX_CLIPBOARD_ITEM_LENGTH", "clipboard item length enforcement")
require(service, "!items.contains(value)", "clipboard deduplication")

# Saved-item storage must retain the JSON store and legacy migration path.
require(store, "SAVED_ITEMS_LIST_KEY", "JSON saved-items key")
require(store, "getStringSet", "legacy saved-items migration")
require(store, "sorted()", "deterministic legacy ordering")
require(store, "linkedSetOf<String>()", "saved-item deduplication")

# My Emoji storage must stay bounded and sanitize persisted values.
require(my_emoji_store, "MAX_CREATIONS = 100", "My Emoji collection size limit")
require(my_emoji_store, "MAX_EMOJI_LENGTH = 64", "My Emoji item length limit")
require(my_emoji_store, "take(MAX_CREATIONS)", "My Emoji persistence cap")
require(my_emoji_store, "distinct()", "My Emoji deduplication")
require(my_emoji_store, "JSONArray", "My Emoji persistent storage")

# Translator wiring must remain present and provider credentials must stay out of the APK.
require(translator_panel, "TranslatorIntegration", "translator integration boundary")
require(translator_panel, "TranslatorPreferences.savePair", "translator preference persistence")
require(translator_panel, "Insert translation", "translator insertion action")
require(translator_integration, "SharedTranslatorExecutor", "shared translator executor")
require(translator_integration, "Handler(Looper.getMainLooper())", "main-thread translator callbacks")
require(translator_service, "UnconfiguredTranslatorProvider", "safe default translator provider")

# The launcher must provide a real enable/select/test path and refresh its
# status when returning from Android input-method settings.
require(main_activity, 'Settings.ACTION_INPUT_METHOD_SETTINGS', "keyboard settings action")
require(main_activity, 'inputMethodManager.showInputMethodPicker()', "keyboard picker action")
require(main_activity, 'testInput.requestFocus()', "keyboard test focus")
require(main_activity, 'inputMethodManager.showSoftInput(testInput, InputMethodManager.SHOW_IMPLICIT)', "keyboard test action")
require(main_activity, 'Settings.Secure.ENABLED_INPUT_METHODS', "enabled keyboard status")
require(main_activity, 'Settings.Secure.DEFAULT_INPUT_METHOD', "selected keyboard status")
require(main_activity, 'override fun onResume()', "launcher status refresh lifecycle")
require(main_activity, 'if (::status.isInitialized) updateStatus()', "safe launcher status refresh")

# Keyboard privacy: the manifest must expose only the input method service,
# and it must be protected by Android's BIND_INPUT_METHOD permission.
for forbidden in (
    "android.permission.INTERNET",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.READ_MEDIA_IMAGES",
    "android.permission.READ_MEDIA_VIDEO",
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
):
    if forbidden in manifest:
        fail(f"unexpected sensitive permission in manifest: {forbidden}")

require(manifest, 'android:permission="android.permission.BIND_INPUT_METHOD"', "protected input-method service")
require(manifest, 'android:name="android.view.im"', "input-method metadata")
require(manifest, 'android:resource="@xml/method"', "input-method configuration resource")
require(method_xml, 'android:imeSubtypeLocale="en_US"', "keyboard locale")
require(method_xml, 'android:imeSubtypeMode="keyboard"', "keyboard subtype mode")

print("Keyboard source verification: PASS")
