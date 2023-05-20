package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ColorDisplay(color: Color, size: Dp = 64.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .drawBehind {
                drawCircle(Color.White, radius = this.size.minDimension * 0.49f)
                drawIntoCanvas {
                    it.drawCheckers()
                }
                drawCircle(color)
            }
    )
}

@Composable
fun ColorDisplay2(color: Color, rounding: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .drawBehind {
                val scale = 0.99f
                val sz = size * scale
                val offset = (1 - scale) * 0.5f
                val topLeft = Offset(size.width * offset, size.height * offset)

                drawRoundRect(
                    Color.White,
                    topLeft = topLeft,
                    size = sz,
                    cornerRadius = CornerRadius(rounding.toPx())
                )
                drawIntoCanvas {
                    it.drawCheckers()
                }
                drawRect(color)
            }
    )
}