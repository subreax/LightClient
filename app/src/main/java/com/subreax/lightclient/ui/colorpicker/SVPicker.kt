package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SVPicker(
    hsv: HsvaColorPickerState,
    modifier: Modifier = Modifier,
    thumbSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .pointerInput(hsv) {
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
                val colorRight = Color.hsv(hsv.h, 1.0f, 1.0f)
                val horizontalBrush = Brush.horizontalGradient(listOf(Color.White, colorRight))
                val verticalBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))

                onDrawBehind {
                    drawRect(horizontalBrush)
                    drawRect(verticalBrush)
                }
            }
            .drawWithContent {
                val oneDp = 1.dp.toPx()
                val color = hsv.toColor(alpha = 1f)
                val radius = thumbSize.toPx() / 2
                val pos = Offset(
                    x = hsv.s * size.width,
                    y = (1 - hsv.v) * size.height
                )

                drawCircle(Color(0x0C000000), radius = radius, center = pos)
                drawCircle(Color.White, radius = radius - oneDp * 2, center = pos)
                drawCircle(color, radius = radius - oneDp * 4, center = pos)
            }
    ) {
        /*Thumb(
            color = Color.Black,
            modifier = Modifier.size(thumbSize).graphicsLayer {
                translationX = hsv.s * containerSize.width - size.width / 2
                translationY = (1 - hsv.v) * containerSize.height - size.height / 2
            }
        )*/
    }
}
