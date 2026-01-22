package com.subreax.lightclient.ui.colorpicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

class ColorPickerState(
    initialColor: Color = Color.White,
    private val onUpdate: (Color) -> Unit
) {
    val hex: HexColorPickerState = HexColorPickerState(
        initialColor = initialColor,
        onUpdate = {
            color = it
            hsva.sync(it)
            onUpdate(it)
        }
    )

    val hsva: HsvaColorPickerState = HsvaColorPickerState(
        initialColor = initialColor,
        onUpdate = {
            color = it
            hex.sync(it)
            onUpdate(it)
        }
    )

    var color by mutableStateOf(initialColor)
        private set

    fun update(newColor: Color) {
        color = newColor
        hsva.sync(newColor)
        hex.sync(newColor)
        onUpdate(newColor)
    }
}

class HexColorPickerState(
    initialColor: Color,
    private val onUpdate: (Color) -> Unit
) {
    var value by mutableStateOf(initialColor.toHexString())

    init {
        sync(initialColor)
    }

    fun toColor(): Color? {
        if (value.length != 8) {
            return null
        }

        val color = value.toLongOrNull(16)
        return color?.let { Color(it) }
    }

    fun update(newValue: String) {
        value = newValue.filter { it.isHexDigit() }
        toColor()?.let { onUpdate(it) }
    }

    fun sync(newColor: Color) {
        value = newColor.toHexString()
    }

    companion object {
        private const val HEX_DIGITS = "0123456789ABCDEF"

        private fun Color.toHexString(): String {
            return buildString {
                arrayOf(
                    (alpha * 255f).roundToInt(),
                    (red * 255f).roundToInt(),
                    (green * 255f).roundToInt(),
                    (blue * 255f).roundToInt()
                ).forEach {
                    append(HEX_DIGITS[(it shr 4) and 0xf])
                    append(HEX_DIGITS[it and 0xf])
                }
            }
        }

        private fun Char.isHexDigit(): Boolean {
            return HEX_DIGITS.contains(this.uppercase())
        }
    }
}

class HsvaColorPickerState(
    initialColor: Color,
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

    init {
        sync(initialColor)
    }

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
}
