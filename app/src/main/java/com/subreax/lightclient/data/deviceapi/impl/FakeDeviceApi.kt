package com.subreax.lightclient.data.deviceapi.impl

import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.ConnectivityObserver
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class FakeDeviceApi(private val connectivityObserver: ConnectivityObserver) : DeviceApi {
    private var _device: Device? = null
    private var _isConnected: Boolean = false
        set(value) {
            field = value
            _flowIsConnected.value = value
        }


    private val _globalProps = listOf(
        Property.StringEnumProperty(0, "Сцена", MutableStateFlow(0), listOf("Smoke")),
        Property.FloatRangeProperty(1, "Яркость", 0.0f, 100.0f, MutableStateFlow(42.0f)),
        Property.ToggleProperty(2, "Датчик движения", MutableStateFlow(true))
    )

    private val _smokeSceneProps = listOf(
        Property.ColorProperty(3, "Цвет", MutableStateFlow(0xff0098ff)),
        Property.FloatRangeProperty(4, "Скорость", 0.0f, 5.0f, MutableStateFlow(1.0f))
    )

    private var _sceneProps = _smokeSceneProps

    override suspend fun connect(device: Device): LResult<Unit> = withContext(Dispatchers.IO) {
        delay(1000)
        if (connectivityObserver.isAvailable) {
            _device = device
            _isConnected = true
            LResult.Success(Unit)
        }
        else {
            LResult.Failure(UiText.Hardcoded("Нет блюпупа"))
        }
    }

    override suspend fun disconnect(): LResult<Unit> {
        _isConnected = false
        _device = null
        return LResult.Success(Unit)
    }

    override fun isConnected(): Boolean {
        return _isConnected
    }

    override fun getDeviceName(): String {
        return _device?.name ?: "unknown"
    }

    override suspend fun getProperties(
        group: DeviceApi.PropertyGroup
    ): LResult<List<Property>> = withContext(Dispatchers.IO) {
        delay(1000)
        when (group) {
            DeviceApi.PropertyGroup.Global -> LResult.Success(_globalProps)
            DeviceApi.PropertyGroup.Scene -> LResult.Success(_sceneProps)
        }
    }

    override suspend fun setPropertyValue(
        property: Property.FloatRangeProperty,
        value: Float
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        delay(100)
        Log.d("FakeDeviceApi", "${property.name} = $value")
        LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.ColorProperty,
        value: Long
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        delay(100)
        Log.d("FakeDeviceApi", "${property.name} = $value")
        LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.ToggleProperty,
        value: Boolean
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        delay(100)
        Log.d("FakeDeviceApi", "${property.name} = $value")
        LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.StringEnumProperty,
        value: Int
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        delay(100)
        Log.d("FakeDeviceApi", "${property.name} = $value")

        if (property.id == 0) {
            _propertiesChanged.emit(DeviceApi.PropertyGroup.Scene)
        }

        LResult.Success(Unit)
    }

    private val _flowIsConnected = MutableStateFlow(_isConnected)
    override val connectionStatus: Flow<Boolean>
        get() = _flowIsConnected

    private val _propertiesChanged = MutableSharedFlow<DeviceApi.PropertyGroup>()
    override val propertiesChanged: Flow<DeviceApi.PropertyGroup>
        get() = _propertiesChanged
}