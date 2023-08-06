package com.subreax.lightclient.data.connection.fake

import com.subreax.lightclient.data.ConnectionType
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.device.Device
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeConnectionRepository : ConnectionRepository {
    override val devices: Flow<List<DeviceDesc>>
        get() = flow {
            emit(listOf(
                DeviceDesc("device1", "address1", ConnectionType.BLE),
                DeviceDesc("device2", "address 2", ConnectionType.BLE)
            ))
        }

    override suspend fun connect(deviceDesc: DeviceDesc) = flow {
        emit(Device.State.Connecting)
        delay(1000)
        emit(Device.State.Fetching)
        delay(1000)
        emit(Device.State.Disconnected)
    }

    override suspend fun disconnect() {

    }
}