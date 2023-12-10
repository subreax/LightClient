package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class World(
    val ox: Float = 0f,
    val oy: Float = 0f,
    val sx: Float = 1f,
    val sy: Float = 1f
) {
    fun worldX2screenX(screenW: Float, wx: Float): Float {
        return wx * screenW * sx + ox * screenW
    }

    fun worldY2screenY(screenH: Float, wy: Float): Float {
        return -oy * screenH - wy * screenH * sy + screenH
    }

    fun screenX2worldX(screenW: Float, x: Float): Float {
        return (x - ox * screenW) / (screenW * sx)
    }

    fun screenY2worldY(screenH: Float, y: Float): Float {
        return -(y - screenH + oy * screenH) / (screenH * sy)
    }

    // todo: add scale limits
    // todo: slow down when scale is big
    fun handlePanZoom(size: IntSize, centroid: Offset, panChange: Offset, zoomChange: Offset): World {
        val x = centroid.x / size.width
        val dx = (x - ox) * (1 - zoomChange.x)

        val y = (centroid.y - size.height) / size.height
        val dy = (-y - oy) * (1 - zoomChange.y)

        return World(
            ox = ox + panChange.x / size.width + dx,
            oy = oy - panChange.y / size.height + dy,
            sx = sx * zoomChange.x,
            sy = sy * zoomChange.y
        )
    }
}