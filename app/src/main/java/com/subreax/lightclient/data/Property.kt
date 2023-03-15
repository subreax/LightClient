package com.subreax.lightclient.data

import kotlinx.coroutines.flow.MutableStateFlow

sealed class Property(val id: Int, val name: String) {
    class StringEnumProperty(
        id: Int,
        name: String,
        val currentValue: MutableStateFlow<Int>,
        val values: List<String>
    ) : Property(id, name)

    class FloatRangeProperty(
        id: Int,
        name: String,
        val min: Float,
        val max: Float,
        val current: MutableStateFlow<Float>
    ) : Property(id, name)

    class ColorProperty(
        id: Int,
        name: String,
        val color: MutableStateFlow<Long>
    ) : Property(id, name)

    class ToggleProperty(
        id: Int,
        name: String,
        val toggled: MutableStateFlow<Boolean>
    ) : Property(id, name)
}
