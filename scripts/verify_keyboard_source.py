from pathlib import Path
import re

SERVICE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/MyEmojiInputMethodService.kt")
STORE = Path("app/src/main/java/com/greyhoundshop073/myemojikeyboard/SavedItemStore.kt")


def fail(message: str) -> None:
    raise SystemExit(f"VERIFY FAILED: {message}")


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        fail(f"missing {label}: {needle}")


service = SERVICE.read_text(encoding="utf-8")
store = STORE.read_text(encoding="utf-8")

# Guard against the duplicate declarations that previously broke compilation.
for declaration in ("private lateinit var root", "private lateinit var content", "private var mode"):
    if len(re.findall(re.escape(declaration), service)) != 1:
        fail(f"expected exactly one declaration of {declaration}")

# Keep every supported keyboard mode wired into rendering.
for mode in ("LETTERS", "EMOJI", "SYMBOLS", "SAVED", "CLIPBOARD"):
    require(service, mode, f"Mode.{mode}")

for function in (
    "renderLetters()",
    "renderEmojis()",
    "renderSymbols()",
    "renderSaved()",
    "renderClipboard()",
    "deletePreviousCharacter()",
    "sendEnter()",
):
    require(service, function, function)

# Safety checks for the current editing and persistence behavior.
require(service, "deleteSurroundingTextInCodePoints(1, 0)", "emoji-aware backspace")
require(service, "sendDefaultEditorAction(true)", "editor-aware enter action")
require(service, "shiftOn = false", "one-shot shift reset")
require(service, "SavedItemStore.saveItem", "saved-item integration")
require(service, "ClipboardManager", "real clipboard integration")

# Saved-item storage must retain the JSON store and legacy migration path.
require(store, "SAVED_ITEMS_LIST_KEY", "JSON saved-items key")
require(store, "getStringSet", "legacy saved-items migration")
require(store, "sorted()", "deterministic legacy ordering")
require(store, "linkedSetOf<String>()", "saved-item deduplication")

print("Keyboard source verification: PASS")
