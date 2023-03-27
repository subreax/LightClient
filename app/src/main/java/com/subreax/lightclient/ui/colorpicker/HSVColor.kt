package com.subreax.lightclient.ui.colorpicker

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

data class HSVColor(
    val h: Float,
    val s: Float,
    val v: Float,
    val a: Float
) {
    fun toColor(alpha: Float = this.a) = Color.hsv(h, s, v, alpha)

    companion object {
        fun from(color: Color): HSVColor {
            val hsv = FloatArray(3)
            android.graphics.Color.RGBToHSV(
                (color.red * 255.0f).roundToInt(),
                (color.green * 255.0f).roundToInt(),
                (color.blue * 255.0f).roundToInt(),
                hsv
            )
            return HSVColor(hsv[0], hsv[1], hsv[2], color.alpha)
        }
    }
}

fun Color.toHsv() = HSVColor.from(this)
