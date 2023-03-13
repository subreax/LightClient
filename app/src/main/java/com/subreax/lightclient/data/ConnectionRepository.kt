package com.subreax.lightclient.data

import com.subreax.lightclient.LResult
import kotlinx.coroutines.flow.Flow


interface ConnectionRepository {
    val devices: Flow<List<Device>>

    suspend fun tryAutoConnect(): Boolean
    suspend fun connect(device: Device): LResult<Unit>
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
}