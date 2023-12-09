package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class Transform(
    var pos: Offset = Offset.Zero,
    var scale: Offset = Offset(1f, 1f)
) {
    fun wx2sx(screenSize: Size, wx: Float): Float {
        //return wx * scale.x * screenSize.width + pos.x

        // aw * x - awX
        val a = scale.x * screenSize.width
        return a * (wx - pos.x)

        //return screenSize.width * scale.x * (wx + pos.x)
    }

    fun wy2sy(screenSize: Size, wy: Float): Float {
        //return -wy * scale.y * screenSize.height + pos.y

        //return -(screenSize.height * scale.y) * (wy + pos.y) + screenSize.height

        // -bh * y + (h + bh * pos.y)
        return -scale.y * screenSize.height * wy + (screenSize.height + scale.y * screenSize.height * pos.y)
    }

    fun sx2wx(screenSize: Size, sx: Float): Float {
        //return (sx - pos.x) / (screenSize.width * scale.x)

        //return sx / (screenSize.width * scale.x) - pos.x
        return sx / (screenSize.width * scale.x) - pos.x
    }


    fun sy2wy(screenSize: Size, sy: Float): Float {
        return sy2wy(screenSize.height, sy)
    }

    fun sx2wx(screenWidth: Float, sx: Float): Float {
        return sx / (screenWidth * scale.x) - pos.x
    }

    fun sy2wy(screenHeight: Float, sy: Float): Float {
        return (screenHeight - sy) / (screenHeight * scale.y) - pos.y
    }
}