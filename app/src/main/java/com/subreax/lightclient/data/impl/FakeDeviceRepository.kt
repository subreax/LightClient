package com.subreax.lightclient.data.impl

import androidx.compose.runtime.mutableStateOf
import com.subreax.lightclient.data.DeviceRepository
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.controllers.SynchronizationController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class FakeDeviceRepository @Inject constructor(
    syncController: SynchronizationController
) : DeviceRepository {
    private val _gprops = mutableListOf<Property>()

    private val _globalProperties = MutableStateFlow<List<Property>>(emptyList())
    override val globalProperties: Flow<List<Property>>
        get() = _globalProperties

    private val _sceneProperties = MutableStateFlow<List<Property>>(emptyList())
    override val sceneProperties: Flow<List<Property>>
        get() = _sceneProperties

    init {
        syncController.addAction {
            delay(1000)

            _gprops.addAll(listOf(
                Property.StringEnumProperty(0, "Сцена", mutableStateOf(0), listOf("Smoke")),
                Property.FloatRangeProperty(1, "Яркость", 0.0f, 100.0f, mutableStateOf(42.0f)),
                Property.ToggleProperty(2, "Датчик движения", mutableStateOf(true))
            ))

            _globalProperties.value = _gprops

            _sceneProperties.value = listOf(
                Property.ColorProperty(3, "Цвет", mutableStateOf(0xff0098ff)),
                Property.FloatRangeProperty(4, "Скорость", 0.0f, 5.0f, mutableStateOf(1.0f))
            )

            true
        }
    }

    override suspend fun getDeviceName(): String {
        return "ESP32-Home"
    }

    override fun setPropertyValue(property: Property.ToggleProperty, value: Boolean) {
        property.toggled.value = value
    }

    override fun setPropertyValue(property: Property.FloatRangeProperty, value: Float) {
        property.current.value = value
    }
}