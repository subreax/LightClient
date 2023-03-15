package com.subreax.lightclient.data

import com.subreax.lightclient.LResult
import kotlinx.coroutines.flow.Flow


interface ConnectionRepository {
    val devices: Flow<List<Device>>

    suspend fun setDevice(device: Device)
    suspend fun connect(): LResult<Unit>
    suspend fun disconnect()
}