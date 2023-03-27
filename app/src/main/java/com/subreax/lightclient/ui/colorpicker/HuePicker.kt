package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


@Composable
fun HuePicker(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    bgModifier: Modifier = Modifier
) {
    DraggableHorizontalSeekbar(
        progress = hue / 360.0f,
        onProgressChanged = { h ->
            onHueChanged(h * 360f)
        },
        thumbColor = Color.hsv(hue, 1.0f, 1.0f),
        modifier = modifier,
        background = {
            Box(
                modifier = bgModifier
                    .clip(CircleShape)
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xffff0000),
                                Color(0xffffff00),
                                Color(0xff00ff00),
                                Color(0xff00ffff),
                                Color(0xff0000ff),
                                Color(0xffff00ff),
                                Color(0xffff0000)
                            )
                        )
                    ),
            )
        }
    )
}
