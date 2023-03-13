package com.subreax.lightclient.data.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.ConnectionRepository
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.data.controllers.SynchronizationController
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.utils.waitFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeConnectionRepository @Inject constructor(
    private val appState: ApplicationState,
    private val syncController: SynchronizationController
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

    override suspend fun tryAutoConnect(): Boolean {
        return false
    }

    override suspend fun connect(device: Device): LResult<Unit> {
        delay(1000)
        appState.notifyEvent(AppEventId.Connected)
        val syncStatus = syncController.syncStatus.waitFor {
            it == SynchronizationController.SyncStatus.Failed ||
                    it == SynchronizationController.SyncStatus.Done
        }

        if (syncStatus == SynchronizationController.SyncStatus.Done) {
            return LResult.Success(Unit)
        }
        appState.notifyEvent(AppEventId.Disconnected)
        return LResult.Failure(syncController.lastError)

        //return AResult.Success(Unit)
        //return AResult.Failure(UiText.Hardcoded("Не удалось подключиться к ${device.name}"))
    }

    override suspend fun disconnect() {

    }

    override suspend fun isConnected(): Boolean {
        return false
    }
}