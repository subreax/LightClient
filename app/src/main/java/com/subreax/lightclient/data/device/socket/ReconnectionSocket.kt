package com.subreax.lightclient.data.device.socket

import com.subreax.lightclient.LResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ReconnectionSocket(private val socket: Socket) : Socket {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(Socket.ConnectionState.Disconnected)
    override val connectionState: StateFlow<Socket.ConnectionState>
        get() = _state

    private var wasConnectionRequested = false

    init {
        coroutineScope.launch {
            socket.connectionState.collect {
                if (wasConnectionRequested && it == Socket.ConnectionState.Disconnected) {
                    connectAsync()
                }
                else {
                    _state.value = it
                }
            }
        }
    }

    override suspend fun connect(): LResult<Unit> {
        wasConnectionRequested = true
        return socket.connect()
    }

    override suspend fun disconnect() {
        wasConnectionRequested = false
        socket.disconnect()
    }

    private fun connectAsync() {
        coroutineScope.launch {
            socket.connect()
        }
    }

    override suspend fun send(data: ByteBuffer) = socket.send(data)
    override suspend fun receive() = socket.receive()
}
