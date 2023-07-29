package com.subreax.lightclient.data.deviceapi.fake

import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import com.subreax.lightclient.data.deviceapi.DeviceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FakeDeviceApi(private val connectivityObserver: ConnectivityObserver) : DeviceApi {
    private var _deviceDesc: DeviceDesc? = null
    private var _isConnected: Boolean = false
        set(value) {
            field = value
            _flowIsConnected.value = value
        }


    private val _globalProps = listOf(
        Property.Enum(0, "Сцена", listOf("Smoke"), 0),
        Property.FloatSlider(1, "Яркость", 0.0f, 100.0f,42.0f),
        Property.Bool(2, "Датчик движения", true)
    )

    private val _smokeSceneProps = listOf(
        Property.Color(3, "Цвет", -16738049),
        Property.FloatSlider(4, "Скорость", 0.0f, 5.0f, 1.0f)
    )

    private var _sceneProps = _smokeSceneProps

    override suspend fun connect(deviceDesc: DeviceDesc): LResult<Unit> = withContext(Dispatchers.IO) {
        delay(1000)
        if (connectivityObserver.isAvailable) {
            _deviceDesc = deviceDesc
            _isConnected = true
            LResult.Success(Unit)
        }
        else {
            LResult.Failure("Нет блюпупа")
        }
    }

    override suspend fun disconnect(): LResult<Unit> {
        _isConnected = false
        _deviceDesc = null
        return LResult.Success(Unit)
    }

    override fun isConnected(): Boolean {
        return _isConnected
    }

    override fun getDeviceName(): String {
        return _deviceDesc?.name ?: "unknown"
    }

    override suspend fun getProperties(
        group: DeviceApi.PropertyGroupId,
        progress: MutableStateFlow<Float>
    ): LResult<List<Property>> = withContext(Dispatchers.IO) {
        delay(1000)
        when (group) {
            DeviceApi.PropertyGroupId.Global -> LResult.Success(_globalProps)
            DeviceApi.PropertyGroupId.Scene -> LResult.Success(_sceneProps)
        }
    }

    override suspend fun updatePropertyValue(property: Property) = withContext(Dispatchers.IO) {
        Log.d("FakeDeviceApi", "update property: $property")
        delay(100)
    }

    private val _flowIsConnected = MutableStateFlow(_isConnected)
    override val connectionStatus: Flow<DeviceApi.ConnectionStatus>
        get() = _flowIsConnected.map { if (it) DeviceApi.ConnectionStatus.Connected else DeviceApi.ConnectionStatus.Disconnected }

    private val _propertiesChanged = MutableSharedFlow<DeviceApi.PropertyGroupId>()
    override val propertiesChanged: Flow<DeviceApi.PropertyGroupId>
        get() = _propertiesChanged
}