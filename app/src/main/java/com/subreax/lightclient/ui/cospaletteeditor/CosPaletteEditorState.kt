package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

class CosPaletteEditorState(
    red: Cosine = Cosine(),
    green: Cosine = Cosine(),
    blue: Cosine = Cosine(),
    private val onCosineDragged: () -> Unit = { }
) {
    enum class CosineId { Red, Green, Blue, Null }

    private val cosines = mutableStateListOf(
        red, green, blue,
        Cosine() // null cosine
    )

    val red: Cosine
        get() = cosines[0]

    val green: Cosine
        get() = cosines[1]

    val blue: Cosine
        get() = cosines[2]

    var selectedCosineId by mutableStateOf(CosineId.Null)
        private set

    var selectedCosine: Cosine
        get() = cosines[selectedCosineId.ordinal]
        set(value) {
            cosines[selectedCosineId.ordinal] = value
        }

    fun select(id: CosineId) {
        selectedCosineId = id
    }

    fun handlePanZoom(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset) {
        val i = selectedCosineId.ordinal
        cosines[i] = cosines[i].handlePanZoom(size, centroid, panChange, zoomChange)
        onCosineDragged()
    }
}

fun Cosine.worldY2screenY(screenH: Float, wy: Float): Float {
    return screenH * (1 - dcOffset - wy * amp)
}

fun Cosine.screenX2worldX(screenW: Float, x: Float): Float {
    return x * freq / screenW + phase
}

// todo: add scale limits
// todo: slow down when scale is big
fun Cosine.handlePanZoom(
    size: IntSize,
    centroid: Offset,
    panChange: Offset,
    zoomChange: Offset
): Cosine {
    val x = centroid.x / size.width * freq
    val dx = x * (1 - zoomChange.x) / zoomChange.x

    val y = (centroid.y - size.height) / size.height
    val dy = (-y - dcOffset) * (1 - zoomChange.y)

    return Cosine(
        phase = phase - panChange.x * freq / size.width - dx,
        dcOffset = dcOffset - panChange.y / size.height + dy,
        freq = freq / zoomChange.x,
        amp = amp * zoomChange.y
    )
}