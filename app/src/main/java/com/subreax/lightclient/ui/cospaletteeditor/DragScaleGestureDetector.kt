package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

suspend fun PointerInputScope.detectDragScaleGestures(
    onEvent: (dx: Float, dy: Float, dsx: Float, dsy: Float) -> Unit
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        while (true) {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (canceled) {
                break
            }

            val panChange = event.calculatePan()

        }
    }
}

suspend fun PointerInputScope.detectTestGestures(
    onGesture: (centroid: Offset, panChange: Offset, zoomChange: Offset) -> Unit
) {
    awaitEachGesture {
        /*var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop*/

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val isConsumed = event.isConsumed()
            if (!isConsumed) {
                val zoomChange2d = event.calculateZoom2d()
                val panChange = event.calculatePan()

                /*if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop || panMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                } else {*/
                    if (zoomChange2d.x != 1f || zoomChange2d.y != 1f || panChange != Offset.Zero) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        onGesture(centroid, panChange, zoomChange2d)
                    }
                    event.changes.forEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                //}
            }
        } while (!isConsumed && event.hasAnyPressed())
    }
}


/*
suspend fun PointerInputScope.detectTestGestures(onMove: (Float, Float) -> Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val first = event.changes.first()
            if (event.type == PointerEventType.Move) {
                val offset = first.position - first.previousPosition
                onMove(offset.x, offset.y)
            }

            Timber.d("${event.type} (${event.changes.size}): ${event.changes.first().position}")
        }
    }
}*/

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
    val dstX = x2 - x1

    val oldY1 = change1.previousPosition.y
    val y1 = change1.position.y
    val oldY2 = change2.previousPosition.y
    val y2 = change2.position.y
    val oldDstY = oldY2 - oldY1
    val dstY = y2 - y1

    return Offset(dstX / oldDstX, dstY / oldDstY)
}
