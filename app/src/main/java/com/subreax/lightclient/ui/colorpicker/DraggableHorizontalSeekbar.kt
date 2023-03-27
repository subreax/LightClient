package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import java.lang.Integer.max
import kotlin.math.roundToInt


@Composable
fun Thumb(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(2.dp, Color(0x0C000000), CircleShape)
            .padding(2.dp)
            .border(2.dp, Color.White, CircleShape)
            .background(color, CircleShape)
    )
}

@Composable
fun DraggableHorizontalSeekbar(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    thumbColor: Color = Color.Transparent,
    background: @Composable () -> Unit
) {
    val currentProgress by rememberUpdatedState(progress.coerceIn(0f, 1f))

    Layout(
        content = {
            background()
            Thumb(color = thumbColor)
        },
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = dragAmount.x / size.width
                    onProgressChanged((currentProgress + delta).coerceIn(0f, 1f))
                }
            }
    ) { measurables, constraints ->
        val minThumbSize = constraints.minHeight

        val thumb = measurables[1].measure(
            constraints.copy(minWidth = minThumbSize, minHeight = minThumbSize)
        )
        val bg = measurables[0].measure(
            constraints.copy(
                maxWidth = constraints.maxWidth - thumb.width,
                minHeight = constraints.minHeight / 2
            )
        )

        val w = bg.width + thumb.width
        val h = max(bg.height, thumb.height)

        layout(w, h) {
            val bgX = thumb.width / 2
            val bgY = (h - bg.height) / 2

            val thumbX = (currentProgress * (w - thumb.width)).roundToInt()
            val thumbY = (h - thumb.height) / 2

            bg.place(bgX, bgY)
            thumb.place(thumbX, thumbY)
        }
    }
}
