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

private const val FunctionStep = 3


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

                    drawCosine(state.red, colors.red, 3f)
                    drawCosine(state.green, colors.green, 3f)
                    drawCosine(state.blue, colors.blue, 3f)
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

class CosPaletteEditorState {
    enum class CosineId { Red, Green, Blue, Null }

    private val cosines = mutableStateListOf(
        Cosine(),
        Cosine(),
        Cosine(),
        Cosine() // null cosine
    )

    var red: Cosine
        get() = cosines[0]
        set(value) {
            cosines[0] = value
        }

    var green: Cosine
        get() = cosines[1]
        set(value) {
            cosines[1] = value
        }

    var blue: Cosine
        get() = cosines[2]
        set(value) {
            cosines[2] = value
        }

    var selectedCosine by mutableStateOf(CosineId.Null)
        private set

    val selectedIdx: Int
        get() = selectedCosine.ordinal

    fun select(id: CosineId) {
        selectedCosine = id
    }

    fun handlePanZoom(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset) {
        val i = selectedIdx
        cosines[i] = cosines[i].handlePanZoom(size, centroid, panChange, zoomChange)
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
                    colors[i] = colors[i].copy(alpha = 0.4f)
                }
            }
            return CosineColors(colors[0], colors[1], colors[2])
        }
    }
}

private fun DrawScope.drawCosine(
    cosine: Cosine,
    color: Color,
    strokeWidth: Float = 1.0f
) {
    val width = size.width.toInt()

    val wx0 = cosine.screenX2worldX(size.width, 0f)
    var y0 = cosine.worldY2screenY(size.height, cos(TwoPi * wx0)).coerceIn(0f, size.height)
    for (x in FunctionStep until width step FunctionStep) {
        val wx1 = cosine.screenX2worldX(size.width, x.toFloat())
        val y1 = cosine.worldY2screenY(size.height, cos(TwoPi * wx1)).coerceIn(0f, size.height)

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