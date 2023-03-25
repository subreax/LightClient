package com.subreax.lightclient.data.connection.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.ApplicationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FakeConnectionRepository @Inject constructor(
    private val appState: ApplicationState,
    private val deviceApi: DeviceApi
) : ConnectionRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val devices: Flow<List<DeviceDesc>> = flow {
        val devices = mutableListOf<DeviceDesc>()
        emit(devices)
        delay(1500)

        devices.add(DeviceDesc("ESP32-Home", "FC:81:CC:4F:8E:36"))
        emit(devices.toList())
        delay(5000)

        devices.add(DeviceDesc("ESP32-Kitchen", "D1:09:75:BA:15:2D"))
        emit(devices.toList())
        delay(5000)

        devices.add(DeviceDesc("ESP32-Bath", "5A:46:70:63:6E:99"))
        emit(devices.toList())
    }

    private var pickedDeviceDesc: DeviceDesc? = null

    init {
        coroutineScope.launch {
            deviceApi.connectionStatus.collect { status ->
                if (status == DeviceApi.ConnectionStatus.ConnectionLost) {
                    appState.notifyEvent(AppEventId.ConnectionLost)
                }
            }
        }
    }

    override suspend fun selectDevice(deviceDesc: DeviceDesc) {
        pickedDeviceDesc = deviceDesc
        appState.notifyEvent(AppEventId.DeviceSelected)
    }

    override suspend fun connect(): LResult<Unit> {
        return pickedDeviceDesc?.let { device ->
            val result = deviceApi.connect(device)
            if (result is LResult.Success) {
                appState.notifyEvent(AppEventId.Connected)
            }
            result
        } ?: LResult.Failure("Устройство не выбрано")
    }

    override suspend fun disconnect() {
        deviceApi.disconnect()
        appState.notifyEvent(AppEventId.Disconnected)
    }
}