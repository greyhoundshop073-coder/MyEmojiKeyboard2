from pathlib import Path
import re

SERVICE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/MyEmojiInputMethodService.kt")
STORE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/SavedItemStore.kt")
MY_EMOJI_STORE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/MyEmojiCreatorStore.kt")
TRANSLATOR_PANEL = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/TranslatorPanel.kt")
TRANSLATOR_INTEGRATION = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/TranslatorIntegration.kt")
TRANSLATOR_SERVICE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/TranslatorService.kt")


def fail(message: str) -> None:
    raise SystemExit(f"VERIFY FAILED: {message}")


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        fail(f"missing {label}: {needle}")


service = SERVICE.read_text(encoding="utf-8")
store = STORE.read_text(encoding="utf-8")
my_emoji_store = MY_EMOJI_STORE.read_text(encoding="utf-8")
translator_panel = TRANSLATOR_PANEL.read_text(encoding="utf-8")
translator_integration = TRANSLATOR_INTEGRATION.read_text(encoding="utf-8")
translator_service = TRANSLATOR_SERVICE.read_text(encoding="utf-8")

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

# Safety checks for the current editing and persistence behavior.
require(service, "deleteSurroundingTextInCodePoints(1, 0)", "emoji-aware backspace")
require(service, "sendDefaultEditorAction(true)", "editor-aware enter action")
require(service, "shiftOn = false", "one-shot shift reset")
require(service, "SavedItemStore.saveItem", "saved-item integration")
require(service, "MyEmojiCreatorStore.getCreations", "My Emoji collection integration")
require(service, "ClipboardManager", "real clipboard integration")

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

print("Keyboard source verification: PASS")
