package com.subreax.lightclient.data.connection.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiText
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

    override val devices: Flow<List<Device>> = flow {
        val devices = mutableListOf<Device>()
        emit(devices)
        delay(1500)

        devices.add(Device("ESP32-Home", "FC:81:CC:4F:8E:36"))
        emit(devices.toList())
        delay(5000)

        devices.add(Device("ESP32-Kitchen", "D1:09:75:BA:15:2D"))
        emit(devices.toList())
        delay(5000)

        devices.add(Device("ESP32-Bath", "5A:46:70:63:6E:99"))
        emit(devices.toList())
    }

    private var pickedDevice: Device? = null

    init {
        coroutineScope.launch {
            deviceApi.connectionStatus.collect { connected ->
                if (!connected && appState.stateIdValue == AppStateId.Ready) {
                    appState.notifyEvent(AppEventId.ConnectionLost)
                }
            }
        }
    }

    override suspend fun setDevice(device: Device) {
        pickedDevice = device
        appState.notifyEvent(AppEventId.DevicePicked)
    }

    override suspend fun connect(): LResult<Unit> {
        return pickedDevice?.let { device ->
            val result = deviceApi.connect(device)
            if (result is LResult.Success) {
                appState.notifyEvent(AppEventId.Connected)
            }
            result
        } ?: LResult.Failure(UiText.Hardcoded("Устройство не выбрано"))
    }

    override suspend fun disconnect() {
        deviceApi.disconnect()
        appState.notifyEvent(AppEventId.Disconnected)
    }
}