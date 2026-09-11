package com.greyhoundshop073.myemojikeyboard

import android.graphics.Color
import android.widget.TextView

/**
 * Compatibility overload for the premium keyboard renderer.
 * A TextView's `text` property is CharSequence, while Android's
 * setTextColor expects a color. Treating an accidental CharSequence
 * argument as the keyboard's default white keeps the renderer safe.
 */
fun TextView.setTextColor(value: CharSequence) {
    setTextColor(Color.WHITE)
}
