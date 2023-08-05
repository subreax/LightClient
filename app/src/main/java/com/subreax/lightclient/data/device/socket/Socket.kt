package com.subreax.lightclient.data.device.socket

import com.subreax.lightclient.LResult
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer

interface Socket {
    enum class ConnectionState {
        Disconnected, NoConnectivity, Connecting, Connected
    }

    val connectionState: StateFlow<ConnectionState>

    suspend fun connect(): LResult<Unit>
    suspend fun disconnect()
    suspend fun send(data: ByteBuffer): LResult<Unit>
    suspend fun receive(): LResult<ByteBuffer>
}
