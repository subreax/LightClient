package com.subreax.lightclient.data.device.fake

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.Device
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class FakeDevice : Device {
    private val _state = MutableStateFlow(Device.State.Disconnected)
    override val state: StateFlow<Device.State>
        get() = _state

    private val _errors = MutableSharedFlow<LResult.Failure>()
    override val errors: Flow<LResult.Failure>
        get() = _errors


    private val _globalProperties = MutableStateFlow(listOf(
        Property.FloatSlider(0, "Brightness", 0.5f, 0f, 1f),
        Property.Color(1, "Color", 0xff9800)
    ))

    override val globalProperties: StateFlow<List<Property>>
        get() = _globalProperties

    private val _sceneProperties = MutableStateFlow(listOf(
        Property.FloatNumber(2, "Scale", 5f, 0f, 10f),
        Property.FloatSmallHSlider(3, "Speed", 0.7f, 0f, 1f)
    ))

    override val sceneProperties: StateFlow<List<Property>>
        get() = _sceneProperties

    override suspend fun connect() = flow {
        _state.value = Device.State.Connecting
        emit(Device.State.Connecting)
        delay(1000)
        _state.value = Device.State.Fetching
        emit(Device.State.Fetching)
        delay(1000)
        _state.value = Device.State.Ready
        emit(Device.State.Ready)
    }

    override suspend fun disconnect() {

    }

    override fun findPropertyById(id: Int): Property? {
        val gp = _globalProperties.value.find { it.id == id }
        if (gp != null) {
            return gp
        }
        return _sceneProperties.value.find { it.id == id }
    }

    override fun getDeviceDesc(): DeviceDesc {
        return DeviceDesc("Fake device", "<address>")
    }
}