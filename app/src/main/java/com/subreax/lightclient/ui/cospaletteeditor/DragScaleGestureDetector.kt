package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

suspend fun PointerInputScope.detectPanZoomGestures(
    onGesture: (centroid: Offset, panChange: Offset, zoomChange: Offset) -> Unit
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val isConsumed = event.isConsumed()
            if (!isConsumed) {
                val zoomChange2d = event.calculateZoom2d()
                val panChange = event.calculatePan()

                if (zoomChange2d.x != 1f || zoomChange2d.y != 1f || panChange != Offset.Zero) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    onGesture(centroid, panChange, zoomChange2d)
                }
                event.changes.forEach {
                    if (it.positionChanged()) {
                        it.consume()
                    }
                }
            }
        } while (!isConsumed && event.hasAnyPressed())
    }
}

private fun PointerEvent.isConsumed(): Boolean {
    return changes.any { it.isConsumed }
}

private fun PointerEvent.hasAnyPressed(): Boolean {
    return changes.any { it.pressed }
}

private fun PointerEvent.calculateZoom2d(): Offset {
    if (changes.size != 2) {
        return Offset(1f, 1f)
    }

    val change1 = changes.first()
    val change2 = changes.last()

    val oldX1 = change1.previousPosition.x
    val x1 = change1.position.x
    val oldX2 = change2.previousPosition.x
    val x2 = change2.position.x
    val oldDstX = oldX2 - oldX1
    var dstX = x2 - x1
    if (abs(dstX) < 75) {
        dstX = oldDstX
    }

    val oldY1 = change1.previousPosition.y
    val y1 = change1.position.y
    val oldY2 = change2.previousPosition.y
    val y2 = change2.position.y
    val oldDstY = oldY2 - oldY1
    var dstY = y2 - y1
    if (abs(dstY) < 75) {
        dstY = oldDstY
    }

    return Offset(dstX / oldDstX, dstY / oldDstY)
}
