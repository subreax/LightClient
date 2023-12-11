package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.ui.graphics.Color

class CosPalette(
    val red: Cosine,
    val green: Cosine,
    val blue: Cosine
) {
    fun getColor(t: Float): Color {
        val r = red.getValue(t)
        val g = green.getValue(t)
        val b = blue.getValue(t)
        return Color(r, g, b)
    }
}