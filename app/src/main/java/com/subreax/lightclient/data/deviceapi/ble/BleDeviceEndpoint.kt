package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.utils.getUtf8String
import com.subreax.lightclient.utils.getWrittenData
import com.subreax.lightclient.utils.toPrettyString
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.GattStatus
import com.welie.blessed.WriteType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder


data class BleResponseHeader(
    val fnId: Byte,
    val userId: Int,
    val status: Byte
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
    private val rw: BluetoothGattCharacteristic
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _eventFlow = MutableSharedFlow<BleLightEvent>()
    val eventFlow: Flow<BleLightEvent>
        get() = _eventFlow

    private val requestBuf = ByteBuffer.allocate(512).apply {
        order(ByteOrder.LITTLE_ENDIAN)
    }

    init {
        callback.addEventListener {
            handleEvent(it)
        }
    }

    suspend fun doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit
    ): LResult<BleResponse> {
        requestBuf.newRequest(fnId)
        onWriteBody(requestBuf)

        val header = writeAndWaitResponseHeader(requestBuf) ?: return LResult.Failure("No header")
        val body = read() ?: return LResult.Failure("No body")
        return if (header.isOk()) {
            LResult.Success(BleResponse(header, body))
        } else {
            val status = header.status
            val errorMsg = try {
                body.getUtf8String()
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
        putInt(System.currentTimeMillis().toInt())
    }

    private fun writeAsync(buf: ByteBuffer) {
        peripheral.writeCharacteristic(
            rw,
            buf.getWrittenData(),
            WriteType.WITHOUT_RESPONSE
        )
    }

    private fun readAsync() {
        peripheral.readCharacteristic(rw)
    }

    private suspend fun read(): ByteBuffer? {
        val deferred = CompletableDeferred<Unit>()
        var body: ByteBuffer? = null

        val listener = callback.addOnReadListener { status, body1 ->
            if (status == GattStatus.SUCCESS) {
                body = body1
            } else {
                Log.e(TAG, "Failed to read characteristic: $status")
            }
            deferred.complete(Unit)
        }

        readAsync()

        try {
            withTimeout(5000) {
                deferred.await()
            }
        } catch (_: TimeoutCancellationException) {
            Log.e(TAG, "read() timeout")
        }

        callback.removeOnReadListener(listener)
        return body
    }

    private suspend fun writeAndWaitResponseHeader(
        buf: ByteBuffer,
        checkHeader: (BleResponseHeader) -> Boolean
    ): BleResponseHeader? {
        val deferred = CompletableDeferred<Unit>()

        var header: BleResponseHeader? = null
        val listener = callback.addOnResponseListener {
            header = parseResponseHeader(it)
            if (header != null && checkHeader(header!!)) {
                deferred.complete(Unit)
            }
        }

        writeAsync(buf)

        try {
            withTimeout(5000) {
                deferred.await()
            }
        } catch (_: TimeoutCancellationException) {
            Log.e(TAG, "writeAndWaitNotification() timeout")
        }

        callback.removeResponseListener(listener)
        return header
    }

    private suspend fun writeAndWaitResponseHeader(buf: ByteBuffer): BleResponseHeader? =
        writeAndWaitResponseHeader(buf) { true }

    private fun parseResponseHeader(data: ByteBuffer): BleResponseHeader? {
        return try {
            val fnId: Byte = data.get()
            val userId: Int = data.getInt()
            val status: Byte = data.get()
            BleResponseHeader(fnId, userId, status)
        } catch (ex: BufferUnderflowException) {
            null
        }
    }

    private fun handleEvent(buf: ByteBuffer) {
        try {
            val notificationId = buf.get().toInt()
            if (notificationId == 0) {
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
    }
}