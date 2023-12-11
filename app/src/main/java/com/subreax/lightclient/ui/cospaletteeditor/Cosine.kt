package com.subreax.lightclient.ui.cospaletteeditor

import kotlin.math.cos

private const val TwoPi = 6.2831855f

data class Cosine(
    val dcOffset: Float,
    val amp: Float,
    val freq: Float,
    val phase: Float
) {
    fun getValue(t: Float): Float {
        val value = dcOffset + amp * cos(TwoPi * (freq * t + phase))
        return value.coerceIn(0f, 1f)
    }
}
