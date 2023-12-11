package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class World(
    val ox: Float = 0f,
    val oy: Float = 0f,
    val sxInv: Float = 1f,
    val sy: Float = 1f
) {
    fun worldY2screenY(screenH: Float, wy: Float): Float {
        return screenH * (1 - oy - wy*sy)
    }

    fun screenX2worldX(screenW: Float, x: Float): Float {
        return x * sxInv / screenW + ox
    }

    // todo: add scale limits
    // todo: slow down when scale is big
    fun handlePanZoom(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset): World {
        val x = centroid.x / size.width * sxInv
        val dx = x * (1 - zoomChange.x) / zoomChange.x

        val y = (centroid.y - size.height) / size.height
        val dy = (-y - oy) * (1 - zoomChange.y)

        return World(
            ox = ox - panChange.x * sxInv / size.width - dx,
            oy = oy - panChange.y / size.height + dy,
            sxInv = sxInv / zoomChange.x,
            sy = sy * zoomChange.y
        )
    }
}