package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.cos

private const val TwoPi = 6.2831855f
private const val FunctionStep = 1


// todo: make it theme-dependent
@Composable
fun CosPaletteEditor(
    modifier: Modifier = Modifier,
    state: CosPaletteEditorState = remember { CosPaletteEditorState() },
    onChange: (() -> Unit) = { }
) {
    val colors = remember(state.selectedCosine) {
        CosineColors.fromState(state)
    }

    Box(
        modifier = modifier
            .onSizeChanged { }
            .drawWithCache {
                onDrawBehind {
                    drawRect(Color(0xff000000))

                    drawCosine(state.cosines[0], colors.red, 2f)
                    drawCosine(state.cosines[1], colors.green, 2f)
                    drawCosine(state.cosines[2], colors.blue, 2f)
                }
            }
            .pointerInput(Unit) {
                detectPanZoomGestures { centroid, panChange, zoomChange ->
                    state.handlePanZoom(size, centroid, panChange, zoomChange)
                    onChange()
                }
            }
    ) { }
}

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

class CosPaletteEditorState {
    enum class CosineId { Red, Green, Blue, Null }

    val cosines = mutableStateListOf(
        World(),
        World(),
        World(),
        World() // null world
    )

    var selectedCosine by mutableStateOf(CosineId.Null)
        private set

    val selectedIdx: Int
        get() = selectedCosine.ordinal

    fun setRed(dcOffset: Float, amp: Float, freq: Float, phase: Float) {
        cosines[0] = createWorld(dcOffset, amp, freq, phase)
    }

    fun getRed() = getCosine(CosineId.Red)

    fun setGreen(dcOffset: Float, amp: Float, freq: Float, phase: Float) {
        cosines[1] = createWorld(dcOffset, amp, freq, phase)
    }

    fun getGreen() = getCosine(CosineId.Green)

    fun setBlue(dcOffset: Float, amp: Float, freq: Float, phase: Float) {
        cosines[2] = createWorld(dcOffset, amp, freq, phase)
    }

    fun getBlue() = getCosine(CosineId.Blue)

    fun select(id: CosineId) {
        selectedCosine = id
    }

    fun handlePanZoom(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset) {
        val i = selectedIdx
        cosines[i] = cosines[i].handlePanZoom(size, centroid, panChange, zoomChange)
    }

    private fun createWorld(dcOffset: Float, amp: Float, freq: Float, phase: Float): World {
        return World(-phase/freq, dcOffset, 1f/freq, amp)
    }

    private fun getCosine(id: CosineId): Cosine {
        return with(cosines[id.ordinal]) {
            Cosine(oy, sy, 1f/sx, -ox/sx)
        }
    }
}

private data class CosineColors(
    val red: Color,
    val green: Color,
    val blue: Color
) {
    companion object {
        fun fromState(state: CosPaletteEditorState): CosineColors {
            val colors = arrayOf(Color.Red, Color.Green, Color.Blue)
            for (i in 0 until 3) {
                if (i != state.selectedIdx) {
                    colors[i] = colors[i].copy(alpha = 0.33333f)
                }
            }
            return CosineColors(colors[0], colors[1], colors[2])
        }
    }
}

private fun DrawScope.drawCosine(
    world: World,
    color: Color,
    strokeWidth: Float = 1.0f
) {
    drawFunction(world, color, strokeWidth) { x -> cos(x * TwoPi) }
}

private fun DrawScope.drawFunction(
    world: World,
    color: Color,
    strokeWidth: Float = 1.0f,
    f: (Float) -> Float
) {
    val width = size.width.toInt()

    val wx0 = world.screenX2worldX(size.width, 0f)
    var y0 = world.worldY2screenY(size.height, f(wx0)).coerceIn(0f, size.height)
    for (x in FunctionStep until width step FunctionStep) {
        val wx1 = world.screenX2worldX(size.width, x.toFloat())
        val y1 = world.worldY2screenY(size.height, f(wx1)).coerceIn(0f, size.height)

        val x0 = (x - FunctionStep).toFloat()
        val x1 = x.toFloat()
        drawLine(
            color = color,
            start = Offset(x0, y0),
            end = Offset(x1, y1),
            strokeWidth = strokeWidth
        )

        y0 = y1
    }
}