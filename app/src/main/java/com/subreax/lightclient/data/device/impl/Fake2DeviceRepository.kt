package com.subreax.lightclient.data.device.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Fake2DeviceRepository : DeviceRepository {
    private val _globalProperties = MutableStateFlow(listOf<Property>(
        Property.Enum(1, "Scene", listOf("Fire"), 0),
        Property.FloatSlider(2, "Brightness", 0f, 1f, 1f),
        Property.IntNumber(3, "Pixels count", 24, 2, 300)
    ))
    override val globalProperties: StateFlow<List<Property>>
        get() = _globalProperties

    private val _sceneProperties = MutableStateFlow(listOf<Property>(
        Property.FloatSlider(4, "Slider", 0f, 1f, 1f),
        Property.FloatSlider(5, "Slider", 0f, 1f, 1f),
        Property.FloatSlider(6, "Slider", 0f, 1f, 1f),
        Property.FloatSlider(7, "Slider", 0f, 1f, 1f),
        Property.FloatSlider(8, "Slider", 0f, 1f, 1f),
        Property.FloatSlider(9, "Slider", 0f, 1f, 1f),
    ))
    override val sceneProperties: StateFlow<List<Property>>
        get() = _sceneProperties

    override suspend fun getDeviceName(): String = "Fake Controller"

    override fun getPropertyById(id: Int): LResult<Property> {
        val v = _globalProperties.value.find { it.id == id }
        if (v != null) {
            return LResult.Success(v)
        }

        val v1 = _sceneProperties.value.find { it.id == id }
        if (v1 != null) {
            return LResult.Success(v1)
        }
        return LResult.Failure("Property not found")
    }

    override fun setPropertyValue(property: Property.Bool, value: Boolean) {
        property.toggled.value = value
    }

    override fun setPropertyValue(property: Property.FloatSlider, value: Float) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.IntNumber, value: Int) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.IntSlider, value: Int) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.Color, value: Int) {
        property.color.value = value
    }

    override fun setPropertyValue(property: Property.Enum, value: Int) {
        property.currentValue.value = value
    }
}