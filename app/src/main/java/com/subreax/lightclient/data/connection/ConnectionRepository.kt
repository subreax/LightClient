package com.subreax.lightclient.data.connection

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Device
import kotlinx.coroutines.flow.Flow


interface ConnectionRepository {
    val devices: Flow<List<Device>>

    suspend fun setDevice(device: Device)
    suspend fun connect(): LResult<Unit>
    suspend fun disconnect()
}