package com.subreax.lightclient.data.connection.fake

import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionProgress
import com.subreax.lightclient.data.connection.ConnectionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeConnectionRepository : ConnectionRepository {
    override val devices: Flow<List<DeviceDesc>>
        get() = flow {
            emit(listOf(
                DeviceDesc("device1", "address1"),
                DeviceDesc("device2", "address 2")
            ))
        }

    override suspend fun connect(deviceDesc: DeviceDesc) = flow {
        emit(ConnectionProgress.Connecting)
        delay(1000)
        emit(ConnectionProgress.Fetching)
        delay(1000)
        emit(ConnectionProgress.FailedToConnect)
    }

    override suspend fun disconnect() {

    }
}