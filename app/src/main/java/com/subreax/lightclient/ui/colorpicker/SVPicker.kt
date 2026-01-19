package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SVPicker(
    hsv: HsvaColorPickerState,
    modifier: Modifier = Modifier,
    thumbSize: Dp = 24.dp
) {
    val colorRight = Color.hsv(hsv.h, 1.0f, 1.0f)

    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    val dx = dragAmount.x / size.width
                    val dy = dragAmount.y / size.height
                    hsv.update(
                        sat = (hsv.s + dx).coerceIn(0f, 1f),
                        value = (hsv.v - dy).coerceIn(0f, 1f)
                    )
                }
            }
            .drawWithCache {
                onDrawBehind {
                    val horizontalBrush = Brush.horizontalGradient(listOf(Color.White, colorRight))
                    val verticalBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                    drawRect(horizontalBrush)
                    drawRect(verticalBrush)
                }
            },
        content = {
            Thumb(
                color = hsv.toColor(alpha = 1.0f),
                modifier = Modifier.size(thumbSize)
            )
        }
    ) { measurables, constraints ->
        val thumb = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))

        val w = constraints.maxWidth
        val h = constraints.maxHeight

        layout(w, h) {
            val x = hsv.s * w - thumb.width / 2
            val y = (1 - hsv.v) * h - thumb.height / 2

            thumb.place(x.roundToInt(), y.roundToInt())
        }
    }
}
