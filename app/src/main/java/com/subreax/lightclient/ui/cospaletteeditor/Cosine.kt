package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.cos

data class Cosine(
    val dcOffset: Float = 0f,
    val amp: Float = 1f,
    val freq: Float = 1f,
    val phase: Float = 0f
) {
    fun worldY2screenY(screenH: Float, wy: Float): Float {
        return screenH * (1 - dcOffset - wy * amp)
    }

    fun screenX2worldX(screenW: Float, x: Float): Float {
        return x * freq / screenW + phase
    }

    // todo: add scale limits
    // todo: slow down when scale is big
    fun handlePanZoom(
        size: IntSize,
        centroid: Offset,
        panChange: Offset,
        zoomChange: Offset
    ): Cosine {
        val x = centroid.x / size.width * freq
        val dx = x * (1 - zoomChange.x) / zoomChange.x

        val y = (centroid.y - size.height) / size.height
        val dy = (-y - dcOffset) * (1 - zoomChange.y)

        return Cosine(
            phase = phase - panChange.x * freq / size.width - dx,
            dcOffset = dcOffset - panChange.y / size.height + dy,
            freq = freq / zoomChange.x,
            amp = amp * zoomChange.y
        )
    }

    fun getValue(t: Float): Float {
        val value = dcOffset + amp * cos(TwoPi * (freq * t + phase))
        return value.coerceIn(0f, 1f)
    }
}