package com.subreax.lightclient.data.connection

import com.subreax.lightclient.data.DeviceDesc
import kotlinx.coroutines.flow.Flow

enum class ConnectionProgress {
    NoConnectivity, Connecting, Fetching, Done, FailedToConnect
}

interface ConnectionRepository {
    val devices: Flow<List<DeviceDesc>>

    suspend fun connect(deviceDesc: DeviceDesc): Flow<ConnectionProgress>
    suspend fun disconnect()
}