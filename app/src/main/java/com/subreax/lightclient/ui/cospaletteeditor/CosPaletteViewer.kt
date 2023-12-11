package com.subreax.lightclient.ui.cospaletteeditor

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

@Composable
fun CosPaletteViewer(
    palette: CosPalette,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf(createBitmap(10, 10)) }

    Box(
        modifier = modifier
            .onSizeChanged {
                bitmap = createBitmap(it.width, it.height)
            }
            .drawWithCache {
                onDrawBehind {
                    palette.drawToBitmap(bitmap)
                    drawImage(bitmap.asImageBitmap())
                }
            }
    ) {}
}

private fun CosPalette.drawToBitmap(bitmap: Bitmap) {
    for (i in 0 until bitmap.width) {
        val t = i.toFloat() / (bitmap.width - 1)
        val color = getColor(t).toArgb()

        for (j in 0 until bitmap.height) {
            bitmap[i, j] = color
        }
    }
}