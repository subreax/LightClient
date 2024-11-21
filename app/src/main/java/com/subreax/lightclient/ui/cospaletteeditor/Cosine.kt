package com.subreax.lightclient.ui.cospaletteeditor

import kotlin.math.cos

data class Cosine(
    val dcOffset: Float = 0f,
    val amp: Float = 1f,
    val freq: Float = 1f,
    val phase: Float = 0f
) {
    fun getValue(t: Float): Float {
        val value = dcOffset + amp * cos(TwoPi * (freq * t + phase))
        return value.coerceIn(0f, 1f)
    }
}
