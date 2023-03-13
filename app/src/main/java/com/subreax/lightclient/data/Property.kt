package com.subreax.lightclient.data

import androidx.compose.runtime.MutableState

sealed class Property(val id: Int, val name: String) {
    class StringEnumProperty(
        id: Int,
        name: String,
        val currentValue: MutableState<Int>,
        val values: List<String>
    ) : Property(id, name)

    class FloatRangeProperty(
        id: Int,
        name: String,
        val min: Float,
        val max: Float,
        val current: MutableState<Float>
    ) : Property(id, name)

    class ColorProperty(
        id: Int,
        name: String,
        val color: MutableState<Long>
    ) : Property(id, name)

    class ToggleProperty(
        id: Int,
        name: String,
        val toggled: MutableState<Boolean>
    ) : Property(id, name)
}
