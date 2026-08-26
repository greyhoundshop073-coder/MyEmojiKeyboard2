package com.greyhoundshop073.myemojikeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.TextView

class MyEmojiInputMethodService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = TextView(this)

        view.text = "My Emoji Keyboard"
        view.textSize = 20f
        view.setPadding(32, 32, 32, 32)

        return view
    }
}
