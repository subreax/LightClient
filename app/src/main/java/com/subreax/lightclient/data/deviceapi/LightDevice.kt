package com.subreax.lightclient.data.deviceapi

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.deviceapi.ble.BleLightEvent
import com.subreax.lightclient.data.deviceapi.ble.FunctionId
import kotlinx.coroutines.flow.SharedFlow
import java.nio.ByteBuffer


abstract class LightDevice {
    data class Request(
        val id: FunctionId,
        val writeBody: ByteBuffer.() -> Unit
    )

    abstract val name: String
    abstract val events: SharedFlow<BleLightEvent>

    abstract suspend fun doRequest(request: Request): LResult<ByteBuffer>
    abstract suspend fun doRequestWithNoResponse(request: Request): LResult<Unit>
    abstract suspend fun disconnect()


    private var _connectionLostListener: (() -> Unit)? = null

    fun setOnConnectionLostListener(onConnectionLost: () -> Unit) {
        _connectionLostListener = onConnectionLost
    }

    protected fun notifyConnectionLost() {
        _connectionLostListener?.let {
            it()
        }
    }
}

