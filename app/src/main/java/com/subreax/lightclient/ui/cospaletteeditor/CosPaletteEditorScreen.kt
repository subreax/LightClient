package com.subreax.lightclient.ui.cospaletteeditor

import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.cos

@Composable
fun CosPaletteEditorScreen() {
    val state = remember {
        CosPaletteState2().also {
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

class CosPaletteEditorState(data12: Array<Float> = Array(12) { 0f }) {
    data class Cosine(val dcOffset: Float, val amp: Float, val freq: Float, val phase: Float)

    var red by mutableStateOf(Cosine(0f, 0f, 0f, 0f))
    var green by mutableStateOf(Cosine(0f, 0f, 0f, 0f))
    var blue by mutableStateOf(Cosine(0f, 0f, 0f, 0f))

    init {
        setData(data12)
    }

    fun setData(data12: Array<Float>) {
        red = Cosine(data12[0], data12[3], data12[6], data12[9])
        green = Cosine(data12[1], data12[4], data12[7], data12[10])
        blue = Cosine(data12[2], data12[5], data12[8], data12[11])
    }
}

class CosPaletteState {
    var transform by mutableStateOf(Transform(
        pos = Offset(0f, 0f),
        scale = Offset(0.5f, 0.5f)
    ))

    fun addOffset(dx: Float, dy: Float) {
        transform = Transform(
            Offset(transform.pos.x + dx, transform.pos.y + dy),
            transform.scale
        )
    }

    fun onGesture(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset) {
        val dx = panChange.x / (transform.scale.x * size.width)
        val dy = -panChange.y / (transform.scale.y * size.height)

        val targetX = transform.sx2wx(size.width.toFloat(), centroid.x)
        val targetY = transform.sy2wy(size.height.toFloat(), centroid.y)
        //Timber.d("target: $targetX   $targetY")

        var posX = transform.pos.x + dx
        posX += (targetX - posX) * (1 - 1 / zoomChange.x)

        var posY = transform.pos.y + dy
        posY += (targetY - posY) * (1 - 1 / zoomChange.y)

        //val posX = targetX - (targetX - (transform.pos.x + dx)) / zoomChange.x
        //val posY = targetY - (targetY - (transform.pos.y + dy)) / zoomChange.y

        transform = Transform(
            Offset(posX, posY),
            Offset(transform.scale.x * zoomChange.x, transform.scale.y * zoomChange.y)
        )
    }
}

class CosPaletteState2 {
    var world by mutableStateOf(World())

    fun applyCosine(dcOffset: Float, amp: Float, freq: Float, phase: Float) {
        world = World(
            phase,
            -dcOffset,
            1 / freq,
            amp
        )
    }
}

// todo: make it theme-dependent
@Composable
fun CosPaletteEditor(
    modifier: Modifier = Modifier,
    state: CosPaletteState2 = remember { CosPaletteState2() }
) {
    Box(
        modifier = modifier
            .onSizeChanged {  }
            .drawWithCache {
                onDrawBehind {
                    drawRect(Color.Black)

                    val circle = Offset(
                        state.world.worldX2screenX(size.width, 0f),
                        state.world.worldY2screenY(size.height, 0f)
                    )
                    drawCircle(Color.Red, 8.0f, circle)

                    val circle2 = Offset(
                        state.world.worldX2screenX(size.width, 1f),
                        state.world.worldY2screenY(size.height, 1f)
                    )
                    drawCircle(Color.Green, 8.0f, circle2)

                    drawCosine(state.world, Color.Red, 2f)
                }
            }
            .pointerInput(Unit) {
                detectTestGestures { centroid, panChange, zoomChange ->
                    val x = centroid.x / size.width
                    val ox = state.world.ox
                    val dx = (x - ox) * (1 - zoomChange.x)

                    val y = (centroid.y - size.height) / size.height
                    val oy = state.world.oy
                    val dy = (y - oy) * (1 - zoomChange.y)

                    state.world = state.world.copy(
                        ox = state.world.ox + panChange.x / size.width + dx,
                        oy = state.world.oy + panChange.y / size.height + dy,
                        sx = state.world.sx * zoomChange.x,
                        sy = state.world.sy * zoomChange.y
                    )
                }
            }
    ) {
        Column {
            Text(text = "origin: ${state.world.ox}   ${state.world.oy}")
            //Text(text = "scale: ${state.transform.scale.x}   ${state.transform.scale.y}")
        }
    }
}

/* private fun DrawScope.drawCosine(
    cosine: CosPaletteEditorState.Cosine,
    color: Color,
    strokeWidth: Float = 1.0f
) {
    drawFunction(color, strokeWidth) { x ->
        cosine.dcOffset + cosine.amp * cos(2f * PI.toFloat() * (cosine.freq * x + cosine.phase))
    }

    drawCircle(Color.Red, 16f, Offset(-cosine.phase / cosine.freq * size.width, cosine.dcOffset * size.height))
}

private fun DrawScope.drawFunction(color: Color, strokeWidth: Float = 1.0f, f: (Float) -> Float) {
    val width = size.width.toInt()
    var x0 = 0f
    var y0 = f(0f).coerceIn(0f, 1f) * size.height
    for (x in 1 until width step 1) {
        val x1 = x.toFloat()
        val y1 = f(x1 / size.width).coerceIn(0f, 1f) * size.height

        drawLine(
            color = color,
            start = Offset(x0, y0),
            end = Offset(x1, y1),
            strokeWidth = strokeWidth
        )

        x0 = x1
        y0 = y1
    }
}*/

private fun DrawScope.drawCosine(
    transform: World,
    color: Color,
    strokeWidth: Float = 1.0f
) {
    drawFunction(transform, color, strokeWidth) { x -> cos(x * PI.toFloat() * 2f) }
}

private const val functionStep = 1

private fun DrawScope.drawFunction(
    transform: World,
    color: Color,
    strokeWidth: Float = 1.0f,
    f: (Float) -> Float
) {
    val width = size.width.toInt()

    val wx0 = transform.screenX2worldX(size.width, 0f)
    var y0 = transform.worldY2screenY(size.height, f(wx0))
    for (x in functionStep until width step functionStep) {
        val wx1 = transform.screenX2worldX(size.width, x.toFloat())
        val y1 = transform.worldY2screenY(size.height, f(wx1))

        drawLine(
            color = color,
            start = Offset((x - functionStep).toFloat(), y0.coerceIn(0f, size.height)),
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