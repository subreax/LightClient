package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.utils.getUtf8String
import com.subreax.lightclient.utils.getWrittenData
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.GattStatus
import com.welie.blessed.WriteType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withTimeout
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

class BleDevice(
    private val peripheral: BluetoothPeripheral,
    private val callback: BleDeviceCallback,
    private val rwCharacteristic: BluetoothGattCharacteristic
) {
    private val _notificationsFlow = MutableSharedFlow<Unit>()
    val notificationFlow: Flow<Unit>
        get() = _notificationsFlow

    private val requestBuf = ByteBuffer.allocate(512).apply {
        order(ByteOrder.LITTLE_ENDIAN)
    }

    suspend fun doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit
    ): LResult<BleResponse> {
        requestBuf.newRequest(fnId)
        onWriteBody(requestBuf)

        val header = writeAndWaitNotification(requestBuf) ?: return LResult.Failure("No header")
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
            rwCharacteristic,
            buf.getWrittenData(),
            WriteType.WITHOUT_RESPONSE
        )
    }

    private fun readAsync() {
        peripheral.readCharacteristic(rwCharacteristic)
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

    private suspend fun writeAndWaitNotification(
        buf: ByteBuffer,
        checkNotification: (BleResponseHeader) -> Boolean
    ): BleResponseHeader? {
        val deferred = CompletableDeferred<Unit>()

        var header: BleResponseHeader? = null
        val listener = callback.addOnNotificationListener {
            header = parseResponseHeader(it)
            if (header != null && checkNotification(header!!)) {
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

        callback.removeNotificationListener(listener)
        return header
    }

    private suspend fun writeAndWaitNotification(buf: ByteBuffer): BleResponseHeader? =
        writeAndWaitNotification(buf) { true }

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

    companion object {
        private const val TAG = "BleDevice"
    }
}