package com.subreax.lightclient.data.device.socket

import com.subreax.lightclient.LResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ReconnectionSocket(private val socket: Socket) : Socket {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(Socket.ConnectionState.Disconnected)
    override val connectionState: StateFlow<Socket.ConnectionState>
        get() = _state

    private var wasConnectionRequested = false
    private val reconnectionRequestChannel = Channel<Unit>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        coroutineScope.launch {
            socket.connectionState.collect {
                if (wasConnectionRequested && it == Socket.ConnectionState.Disconnected) {
                    reconnectionRequestChannel.send(Unit)
                }
                else {
                    _state.value = it
                }
            }
        }

        coroutineScope.launch {
            while (isActive) {
                reconnectionRequestChannel.receive()
                if (socket.connectionState.value == Socket.ConnectionState.Disconnected) {
                    connect()
                    delay(2000)
                }
            }
        }
    }

    override suspend fun connect(): LResult<Unit> {
        val res = socket.connect()
        if (res is LResult.Success) {
            wasConnectionRequested = true
        }
        return res
    }

    override suspend fun disconnect() {
        wasConnectionRequested = false
        socket.disconnect()
    }

    override suspend fun send(data: ByteBuffer) = socket.send(data)
    override suspend fun receive() = socket.receive()
}
