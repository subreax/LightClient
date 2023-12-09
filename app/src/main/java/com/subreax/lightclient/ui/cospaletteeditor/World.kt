package com.subreax.lightclient.ui.cospaletteeditor

data class World(
    var ox: Float = 0f,
    var oy: Float = 0f,
    var sx: Float = 1f,
    var sy: Float = 1f
) {
    fun worldX2screenX(screenW: Float, wx: Float): Float {
        return wx * screenW * sx + ox * screenW
    }

    fun worldY2screenY(screenH: Float, wy: Float): Float {
        return oy * screenH - wy * screenH * sy + screenH
    }

    fun screenX2worldX(screenW: Float, x: Float): Float {
        return (x - ox * screenW) / (screenW * sx)
    }

    fun screenY2worldY(screenH: Float, y: Float): Float {
        return -(y - screenH - oy * screenH) / (screenH * sy)
    }
}