package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

@Composable
fun AlphaPicker(
    hsv: HsvaColorPickerState,
    alpha: Float,
    onAlphaChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    DraggableHorizontalSeekbar(
        progress = alpha,
        onProgressChanged = onAlphaChanged,
        modifier = modifier,
        thumbColor = lerp(Color.White, hsv.toColor(), alpha)
    ) {
        Box(
            Modifier
                .clip(CircleShape)
                .fillMaxWidth()
                .drawBehind {
                    drawRect(Color.White)
                    drawIntoCanvas {
                        it.drawCheckers()
                    }
                }
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            hsv.toColor(alpha = 1.0f)
                        )
                    )
                ),
        )
    }
}
