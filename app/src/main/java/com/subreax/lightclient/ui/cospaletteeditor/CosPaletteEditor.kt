package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos

private const val FunctionStep = 3


// todo: make it theme-dependent
@Composable
fun CosPaletteEditor(
    modifier: Modifier = Modifier,
    state: CosPaletteEditorState = remember { CosPaletteEditorState() }
) {
    val colors = remember(state.selectedCosineId) {
        CosineColors.create(state.selectedCosineId)
    }

    Box(
        modifier = modifier
            .focusable()
            .drawBehind {
                drawRect(Color.Black)

                drawCosine(state.red, colors.red, 3f)
                drawCosine(state.green, colors.green, 3f)
                drawCosine(state.blue, colors.blue, 3f)
            }
            .pointerInput(Unit) {
                detectPanZoomGestures { centroid, panChange, zoomChange ->
                    state.handlePanZoom(size, centroid, panChange, zoomChange)
                }
            }
    ) { }
}

private data class CosineColors(
    val red: Color,
    val green: Color,
    val blue: Color
) {
    companion object {
        fun create(selectedCosineId: CosPaletteEditorState.CosineId): CosineColors {
            val colors = arrayOf(Color.Red, Color.Green, Color.Blue)
            for (i in 0 until 3) {
                if (i != selectedCosineId.ordinal) {
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