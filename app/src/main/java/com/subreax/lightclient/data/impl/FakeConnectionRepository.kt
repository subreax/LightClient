package com.subreax.lightclient.data.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.ConnectionRepository
import com.subreax.lightclient.data.ConnectivityObserver
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeConnectionRepository @Inject constructor(
    private val appState: ApplicationState,
    private val connectivityObserver: ConnectivityObserver
) : ConnectionRepository {
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

    override suspend fun setDevice(device: Device) {
        pickedDevice = device
        appState.notifyEvent(AppEventId.DevicePicked)
    }

    override suspend fun connect(): LResult<Unit> {
        delay(1000)
        if (connectivityObserver.isAvailable) {
            return LResult.Success(Unit)
        }
        return LResult.Failure(UiText.Hardcoded("Нет блюпупа"))
    }

    override suspend fun disconnect() {
        delay(500)
        appState.notifyEvent(AppEventId.Disconnected)
    }
}