package com.subreax.lightclient.ui.colorpicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastRoundToInt
import kotlin.math.round
import kotlin.math.roundToInt

class ColorPickerState(
    initialColor: Color = Color.Companion.White,
    private val onUpdate: (Color) -> Unit
) {
    val rgbaStr: RgbaColorPickerState = RgbaColorPickerState(onUpdate = {
        color = it
        hsva.sync(it)
        onUpdate(it)
    })

    val hsva: HsvaColorPickerState = HsvaColorPickerState(onUpdate = {
        color = it
        rgbaStr.sync(it)
        onUpdate(it)
    })

    var color by mutableStateOf(initialColor)
        private set

    init {
        rgbaStr.sync(color)
        hsva.sync(color)
    }
}

class RgbaColorPickerState(
    private val onUpdate: (Color) -> Unit
) {
    var r by mutableStateOf("")
        private set

    var g by mutableStateOf("")
        private set

    var b by mutableStateOf("")
        private set

    var a by mutableStateOf("")
        private set

    fun toColor(): Color? {
        val ri = r.toIntOrNull() ?: return null
        val gi = g.toIntOrNull() ?: return null
        val bi = b.toIntOrNull() ?: return null
        val ai = a.toIntOrNull() ?: return null
        return Color(ri, gi, bi, ai)
    }

    fun update(red: String = r, green: String = g, blue: String = b, alpha: String = a) {
        r = red
        g = green
        b = blue
        a = alpha
        toColor()?.let(onUpdate)
    }

    fun sync(newColor: Color) {
        r = (newColor.red * 255).fastRoundPositiveToInt().toString()
        g = (newColor.green * 255).fastRoundPositiveToInt().toString()
        b = (newColor.blue * 255).fastRoundPositiveToInt().toString()
        a = (newColor.alpha * 255).fastRoundPositiveToInt().toString()
    }

    companion object {
        fun Float.fastRoundPositiveToInt(): Int {
            return (this + 0.5f).toInt()
        }
    }
}

class HsvaColorPickerState(
    private val onUpdate: (Color) -> Unit
) {
    var h by mutableFloatStateOf(0f)
        private set

    var s by mutableFloatStateOf(0f)
        private set

    var v by mutableFloatStateOf(0f)
        private set

    var a by mutableFloatStateOf(0f)
        private set

    fun update(hue: Float = h, sat: Float = s, value: Float = v, alpha: Float = a) {
        h = hue
        s = sat
        v = value
        a = alpha
        onUpdate(toColor())
    }

    fun sync(newColor: Color) {
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (newColor.red * 255.0f).roundToInt(),
            (newColor.green * 255.0f).roundToInt(),
            (newColor.blue * 255.0f).roundToInt(),
            hsv
        )
        h = hsv[0]
        s = hsv[1]
        v = hsv[2]
        a = newColor.alpha
    }

    fun toColor(): Color {
        return Color.hsv(h, s, v, a)
    }

    fun toColor(alpha: Float): Color {
        return Color.hsv(h, s, v, alpha)
    }

    companion object {
        fun from(color: Color, onUpdate: (Color) -> Unit): HsvaColorPickerState {
            return HsvaColorPickerState(onUpdate).apply { sync(color) }
        }
    }
}
