package com.subreax.lightclient.ui.colorpicker

import androidx.compose.ui.graphics.*
import kotlin.math.ceil

fun Canvas.drawCheckers(
    color: Color = Color(0x2A7F7F7F)
) {
    val w = nativeCanvas.width.toFloat()
    val h = nativeCanvas.height.toFloat()

    val cellSizePx = 11f

    val xCount = ceil(w / cellSizePx).toInt()
    val yCount = ceil(h / cellSizePx).toInt()

    val paint = Paint().also {
        it.style = PaintingStyle.Fill
        it.color = color
    }

    for (yi in 0 until yCount) {
        for (xi in 0 until xCount) {
            if ((xi + yi) % 2 == 0) {
                val x = xi * cellSizePx
                val y = yi * cellSizePx
                drawRect(x, y, x + cellSizePx, y + cellSizePx, paint)
            }
        }
    }
}

fun lerp(a: Float, b: Float, value: Float): Float {
    return a + (b - a) * value
}

fun lerp(a: Color, b: Color, value: Float): Color {
    return Color(
        red = lerp(a.red, b.red, value),
        green = lerp(a.green, b.green, value),
        blue = lerp(a.blue, b.blue, value),
        alpha = 1f
    )
}

