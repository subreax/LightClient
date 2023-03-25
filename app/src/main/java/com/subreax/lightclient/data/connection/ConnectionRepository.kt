package com.subreax.lightclient.data.connection

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import kotlinx.coroutines.flow.Flow


interface ConnectionRepository {
    val devices: Flow<List<DeviceDesc>>

    suspend fun selectDevice(deviceDesc: DeviceDesc)
    suspend fun connect(): LResult<Unit>
    suspend fun disconnect()
}