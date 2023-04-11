package com.subreax.lightclient.data

import kotlinx.coroutines.flow.MutableStateFlow

enum class PropertyType {
    FloatSlider, Color, Enum, Int, IntSlider, Toggle, Special
}

sealed class Property(val id: Int, val type: PropertyType, val name: String) {
    class SpecLoading(
        initialProgress: Float
    ) : Property(-1, PropertyType.Special, "Loading in progress") {
        val progress = MutableStateFlow(initialProgress)
    }


    class Enum(
        id: Int,
        name: String,
        val values: List<String>,
        initialValue: Int,
    ) : Property(id, PropertyType.Enum, name) {
        val currentValue = MutableStateFlow(initialValue)
    }

    class FloatSlider(
        id: Int,
        name: String,
        val min: Float,
        val max: Float,
        initialValue: Float
    ) : Property(id, PropertyType.FloatSlider, name) {
        val current = MutableStateFlow(initialValue)
    }

    class Color(
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

    open class BaseIntProperty(
        id: Int,
        type: PropertyType,
        name: String,
        initialValue: Int,
        val min: Int,
        val max: Int
    ) : Property(id, type, name) {
        val current = MutableStateFlow(initialValue)
    }

    class IntNumber(
        id: Int,
        name: String,
        initialValue: Int,
        min: Int,
        max: Int
    ) : BaseIntProperty(id, PropertyType.Int, name, initialValue, min, max)

    class IntSlider(
        id: Int,
        name: String,
        initialValue: Int,
        min: Int,
        max: Int
    ) : BaseIntProperty(id, PropertyType.IntSlider, name, initialValue, min, max)
}
