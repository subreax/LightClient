package com.subreax.lightclient.data

import kotlinx.coroutines.flow.MutableStateFlow

enum class PropertyType {
    FloatRange, Color, StringEnum, Toggle
}

sealed class Property(val id: Int, val type: PropertyType, val name: String) {
    class StringEnumProperty(
        id: Int,
        name: String,
        val values: List<String>,
        initialValue: Int,
    ) : Property(id, PropertyType.StringEnum, name) {
        val currentValue = MutableStateFlow(initialValue)
    }

    class FloatRangeProperty(
        id: Int,
        name: String,
        val min: Float,
        val max: Float,
        initialValue: Float
    ) : Property(id, PropertyType.FloatRange, name) {
        val current = MutableStateFlow(initialValue)
    }

    class ColorProperty(
        id: Int,
        name: String,
        initialValue: Int
    ) : Property(id, PropertyType.Color, name) {
        val color = MutableStateFlow(initialValue)
    }

    class ToggleProperty(
        id: Int,
        name: String,
        initialValue: Boolean
    ) : Property(id, PropertyType.Toggle, name) {
        val toggled = MutableStateFlow(initialValue)
    }
}
