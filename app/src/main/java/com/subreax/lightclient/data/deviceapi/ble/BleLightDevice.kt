package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.deviceapi.LightDevice
import com.subreax.lightclient.utils.getWrittenData
import com.subreax.lightclient.utils.toPrettyString
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.ConnectionState
import com.welie.blessed.WriteType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private data class RequestWrapper(
    val data: LightDevice.Request,
    val onResponse: (LResult<ByteBuffer>) -> Unit,
    val isVoid: Boolean = false
)

private data class LightResponseHeader(
    val fnId: Int,
    val status: Int,
    val packetsCount: Int,
    val bodySz: Int
)

class BleLightDevice(
    private val peripheral: BluetoothPeripheral,
    private val callback: BleDeviceCallback,
    private val requestCharacteristic: BluetoothGattCharacteristic
) : LightDevice() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val requestChannel = Channel<RequestWrapper>(Channel.UNLIMITED)

    private val requestBuf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)
    private val responseBuf = ByteBuffer.allocate(PACKET_SZ * 819).order(ByteOrder.LITTLE_ENDIAN)

    private val _events = MutableSharedFlow<BleLightEvent>()
    override val events: SharedFlow<BleLightEvent>
        get() = _events

    override val name: String
        get() = peripheral.name

    private var connectionLostDetectorJob: Job = Job()
    private var eventReceiverJob: Job = Job()
    private var requestQueueJob: Job = Job()

    init {
        startConnectionLostDetector()
        startEventReceiver()
        startRequestQueue()
    }

    private fun startConnectionLostDetector() {
        connectionLostDetectorJob = coroutineScope.launch {
            while (isActive) {
                if (peripheral.state != ConnectionState.CONNECTED) {
                    onDisconnect()
                    notifyConnectionLost()
                    cancel()
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun onDisconnect() {
        connectionLostDetectorJob.cancel()
        eventReceiverJob.cancel()
        requestQueueJob.cancel()
    }

    private fun startEventReceiver() {
        eventReceiverJob = coroutineScope.launch {
            try {
                callback.receiveEvents = true
                while (isActive) {
                    handleEvent(callback.eventChannel.receive())
                }
            } finally {
                callback.receiveEvents = false
            }
        }
    }

    private suspend fun handleEvent(buf: ByteBuffer) {
        try {
            val eventId = buf.get().toInt()
            if (eventId == 0) {
                val groupId = buf.get().toInt()
                val group = DeviceApi.PropertyGroup.values()[groupId]

                Log.v(TAG, "### new event: Properties Changed")
                _events.emit(BleLightEvent.PropertiesChanged(group))
            }
        } catch (ex: BufferUnderflowException) {
            Log.e(TAG, "Failed to handle event ${buf.toPrettyString(0, buf.limit())}")
        }
    }

    private fun startRequestQueue() {
        requestQueueJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                val request = requestChannel.receive()
                Log.v(TAG, "--> running request ${request.data.id}")
                performRequest(request)
                Log.v(TAG, "<-- finished request ${request.data.id}")
            }
        }
    }

    override suspend fun doRequest(
        request: Request
    ): LResult<ByteBuffer> = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val requestWrapper = RequestWrapper(
                data = request,
                onResponse = { cont.resume(it) }
            )

            Log.v(TAG, "+++ enqueue new request ${request.id}")
            requestChannel.trySend(requestWrapper)
        }
    }

    override suspend fun doRequestWithNoResponse(
        request: Request
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val result = suspendCoroutine { cont ->
            val requestWrapper = RequestWrapper(
                data = request,
                onResponse = { cont.resume(it) },
                isVoid = true
            )

            Log.v(TAG, "+++ enqueue new request with no response ${request.id}")
            requestChannel.trySend(requestWrapper)
        }

        if (result is LResult.Success) {
            LResult.Success(Unit)
        } else {
            result as LResult.Failure
        }
    }

    override suspend fun disconnect() {
        onDisconnect()
        peripheral.cancelConnection()
    }

    private suspend fun performRequest(request: RequestWrapper) = withContext(Dispatchers.IO) {
        writeRequest(requestBuf, request.data)

        callback.receivePackets = true
        if (!sendRequestBuf()) {
            callback.receivePackets = false
            Log.d(TAG, "Failed to write request")
            return@withContext
        }

        val response = if (!request.isVoid) {
            receiveResponse()
        } else {
            LResult.Success(ByteBuffer.wrap(requestBuf.array(), 0, 1))
        }

        callback.receivePackets = false
        request.onResponse(response)
    }

    private fun writeRequest(buffer: ByteBuffer, request: Request) {
        requestBuf.clear()
        writeHeader(buffer, request.id)
        writeBody(buffer, request)
    }

    private fun writeHeader(buffer: ByteBuffer, fnId: FunctionId) {
        buffer.put(fnId.ordinal.toByte())
    }

    private fun writeBody(buffer: ByteBuffer, request: Request) {
        request.writeBody(buffer)
    }

    private fun sendRequestBuf(): Boolean {
        return peripheral.writeCharacteristic(
            requestCharacteristic,
            requestBuf.getWrittenData(),
            WriteType.WITHOUT_RESPONSE
        )
    }

    private suspend fun receiveResponse(): LResult<ByteBuffer> {
        responseBuf.clear()

        var header: LightResponseHeader? = null
        var packetsCount = 1
        var packetsReceived = 0

        try {
            while (packetsReceived < packetsCount) {
                val packet = withTimeout(1000) {
                    callback.packetChannel.receive()
                }

                if (packetsReceived == 0) {
                    header = parseResponseHeader(packet)
                    packet.position(0)
                    if (header == null) {
                        return LResult.Failure(R.string.failed_to_parse_header)
                    }
                    packetsCount = header.packetsCount
                }

                responseBuf.put(packet)
                packetsReceived++
            }
        } catch (ex: TimeoutCancellationException) {
            return if (packetsReceived == 0) {
                LResult.Failure("Failed to receive response")
            } else {
                LResult.Failure("Failed to receive response: ${packetsReceived + 1}/$packetsCount")
            }
        }

        val bodySz = header!!.bodySz
        val body = ByteBuffer
            .wrap(responseBuf.array(), 6, bodySz)
            .order(ByteOrder.LITTLE_ENDIAN)

        return LResult.Success(body)
    }

    private fun parseResponseHeader(packet: ByteBuffer): LightResponseHeader? {
        return try {
            val fnId = packet.get().toInt()
            val status = packet.get().toInt()
            val packetsCount = packet.getShort().toInt()
            val bodySz = packet.getShort().toInt()
            LightResponseHeader(fnId, status, packetsCount, bodySz)
        } catch (ex: BufferUnderflowException) {
            null
        }
    }

    companion object {
        private const val TAG = "BleLightDevice"
        private const val PACKET_SZ = 20
    }
}