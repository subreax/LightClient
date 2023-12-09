package com.subreax.lightclient.ui.cospaletteeditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round

private const val FunctionStep = 1

@Composable
fun CosPaletteEditorScreen() {
    val state = remember {
        CosPaletteState().also {
            it.applyCosine(0.5f, 0.5f, 0.8f, 0f)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Цветовая палитра",
            subtitle = {
                Text(text = "Зажги привычные эффекты по-новому")
            },
            navBack = {
                // todo: not implemented
            }
        )

        CosPaletteEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
        )
    }
}

class CosPaletteState {
    var world by mutableStateOf(World())

    fun applyCosine(dcOffset: Float, amp: Float, freq: Float, phase: Float) {
        world = World(
            phase,
            dcOffset,
            1 / freq,
            amp
        )
    }

    fun handlePanZoom(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset) {
        world = world.handlePanZoom(size, centroid, panChange, zoomChange)
    }
}

// todo: make it theme-dependent
@Composable
fun CosPaletteEditor(
    modifier: Modifier = Modifier,
    state: CosPaletteState = remember { CosPaletteState() }
) {
    Box(
        modifier = modifier
            .onSizeChanged { }
            .drawWithCache {
                onDrawBehind {
                    drawRect(Color(0xff000000))

                    val circle = Offset(
                        state.world.worldX2screenX(size.width, 0f),
                        state.world.worldY2screenY(size.height, 0f)
                    )
                    drawCircle(Color.Red, 8.0f, circle)

                    drawCosine(state.world, Color.Red, 2f)
                }
            }
            .pointerInput(Unit) {
                detectPanZoomGestures { centroid, panChange, zoomChange ->
                    state.handlePanZoom(size, centroid, panChange, zoomChange)
                }
            }
    ) {
        Column {
            Text(text = "origin: ${state.world.ox.round4()}   ${state.world.oy.round4()}")
            Text(text = "scale:  ${state.world.sx.round4()}   ${state.world.sy.round4()}")
        }
    }
}

private fun Float.round4(): Float {
    return round(this * 10000) / 10000f
}

private fun DrawScope.drawCosine(
    transform: World,
    color: Color,
    strokeWidth: Float = 1.0f
) {
    drawFunction(transform, color, strokeWidth) { x -> cos(x * PI.toFloat() * 2f) }
}

private fun DrawScope.drawFunction(
    transform: World,
    color: Color,
    strokeWidth: Float = 1.0f,
    f: (Float) -> Float
) {
    val width = size.width.toInt()

    val wx0 = transform.screenX2worldX(size.width, 0f)
    var y0 = transform.worldY2screenY(size.height, f(wx0))
    for (x in FunctionStep until width step FunctionStep) {
        val wx1 = transform.screenX2worldX(size.width, x.toFloat())
        val y1 = transform.worldY2screenY(size.height, f(wx1))

        drawLine(
            color = color,
            start = Offset((x - FunctionStep).toFloat(), y0.coerceIn(0f, size.height)),
            end = Offset(x.toFloat(), y1.coerceIn(0f, size.height)),
            strokeWidth = strokeWidth
        )

        y0 = y1
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 320, heightDp = 200)
@Composable
fun CosPaletteEditorPreview() {
    LightClientTheme {
        Surface {
            CosPaletteEditor(Modifier.fillMaxSize())
        }
    }
}