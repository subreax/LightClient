package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.utils.getUtf8String
import com.subreax.lightclient.utils.getWrittenData
import com.subreax.lightclient.utils.toPrettyString
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.WriteType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder


data class BleResponseHeader(
    val fnId: Byte,
    val status: Byte,
    val packetsCount: Int,
    val bodySz: Int
) {
    fun isOk(): Boolean = status.toInt() == 0
}

data class BleResponse(
    val header: BleResponseHeader,
    val body: ByteBuffer
)

class BleDeviceEndpoint(
    private val peripheral: BluetoothPeripheral,
    private val callback: BleDeviceCallback,
    private val req: BluetoothGattCharacteristic
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _eventFlow = MutableSharedFlow<BleLightEvent>()
    val eventFlow: Flow<BleLightEvent>
        get() = _eventFlow

    private val requestBuf = ByteBuffer.allocate(512).apply {
        order(ByteOrder.LITTLE_ENDIAN)
    }

    private val responseBuf = ByteBuffer.allocate(PACKET_SZ * 819).apply {
        order(ByteOrder.LITTLE_ENDIAN)
    }

    private var eventListener: OnEventListener? = null

    fun setEventListener() {
        eventListener = callback.addEventListener {
            handleEvent(it)
        }
    }

    fun cancelEventListener() {
        eventListener?.let {
            callback.removeEventListener(it)
        }
    }

    suspend fun doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit
    ): LResult<BleResponse> {
        requestBuf.newRequest(fnId)
        onWriteBody(requestBuf)
        val response = writeAndWaitResponse(requestBuf)
            ?: return LResult.Failure("Failed to fetch response")

        return if (response.header.isOk()) {
            LResult.Success(response)
        } else {
            val status = response.header.status
            val errorMsg = try {
                response.body.getUtf8String()
            } catch (ex: Exception) {
                "<empty>"
            }
            LResult.Failure("Failed to perform request. Status: $status  msg: $errorMsg")
        }
    }

    fun doRequestWithoutResponse(fnId: FunctionId, onWriteBody: ByteBuffer.() -> Unit) {
        requestBuf.newRequest(fnId)
        onWriteBody(requestBuf)
        writeAsync(requestBuf)
    }

    private fun ByteBuffer.newRequest(fnId: FunctionId) {
        clear()
        setHeader(fnId)
    }

    private fun ByteBuffer.setHeader(fnId: FunctionId) {
        put(fnId.ordinal.toByte())
    }

    private fun writeAsync(buf: ByteBuffer) {
        peripheral.writeCharacteristic(
            req,
            buf.getWrittenData(),
            WriteType.WITHOUT_RESPONSE
        )
    }

    private suspend fun writeAndWaitResponse(buf: ByteBuffer): BleResponse? {
        val packetChannel = Channel<ByteBuffer>(Channel.UNLIMITED)
        val listener = callback.addOnPacketListener {
            it.limit(PACKET_SZ)
            packetChannel.trySend(it)
        }

        responseBuf.position(0)
        writeAsync(buf)

        var packetsCount = 1
        var packetsReceived = 0
        var header: BleResponseHeader? = null
        while (packetsReceived < packetsCount) {
            try {
                val packet = withTimeout(1000) {
                    packetChannel.receive()
                }

                if (packetsReceived == 0) {
                    // reads header from packet. after this packet will contain only body data
                    header = parseResponseHeader(packet)
                    if (header == null) {
                        Log.e(TAG, "Failed to parse header")
                        break
                    }
                    packetsCount = header.packetsCount
                }

                responseBuf.put(packet)
                ++packetsReceived
                //Log.v(TAG, "Packed received: $packetsReceived/$packetsCount")
            } catch (_: TimeoutCancellationException) {
                Log.e(TAG, "Failed to fetch new packet")
                break
            }
        }

        callback.removeResponseListener(listener)
        val bodySz = header?.bodySz ?: 0
        val body = ByteBuffer
            .wrap(responseBuf.array(), 0, bodySz)
            .order(ByteOrder.LITTLE_ENDIAN)
        return if (header != null) BleResponse(header, body) else null
    }

    private fun parseResponseHeader(data: ByteBuffer): BleResponseHeader? {
        return try {
            val fnId: Byte = data.get()
            val status: Byte = data.get()
            val packetsCount = data.getShort().toInt()
            val bodySz = data.getShort().toInt()
            BleResponseHeader(fnId, status, packetsCount, bodySz)
        } catch (ex: BufferUnderflowException) {
            null
        }
    }

    private fun handleEvent(buf: ByteBuffer) {
        try {
            val eventId = buf.get().toInt()
            if (eventId == 0) {
                val groupId = buf.get().toInt()
                val group = DeviceApi.PropertyGroup.values()[groupId]
                coroutineScope.launch {
                    _eventFlow.emit(BleLightEvent.PropertiesChanged(group))
                }
            }
        } catch (ex: BufferUnderflowException) {
            Log.e(TAG, "Failed to handle event ${buf.toPrettyString()}")
        }
    }

    companion object {
        private const val TAG = "BleDeviceEndpoint"
        private const val PACKET_SZ = 20
    }
}