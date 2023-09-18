package com.subreax.lightclient.data.device.socket

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.device.api.Event
import com.subreax.lightclient.data.device.repo.PropertyGroup
import com.subreax.lightclient.utils.toPrettyString
import com.subreax.lightclient.utils.waitForWithTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.resume


private data class RequestWrapper(
    val data: SocketWrapper.Request,
    val onResponse: (LResult<ByteBuffer>) -> Unit,
    val isVoid: Boolean = false
)

private class LightMessage(
    val fnId: Int,
    val status: Int,
    bodyBuf: ByteBuffer
) {
    private val arr = bodyBuf.array()
    private val offset = bodyBuf.position()
    private val len = bodyBuf.remaining()

    val isEvent: Boolean
        get() = fnId == 255

    val isResponse: Boolean
        get() = fnId != 255

    fun body(): ByteBuffer = ByteBuffer
        .wrap(arr, offset, len)
        .order(ByteOrder.LITTLE_ENDIAN)
}

// todo: rename
class SocketWrapper(private val socket: Socket) {
    data class Request(
        val id: Int,
        val writeBody: ByteBuffer.() -> Unit
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val requestChannel = Channel<RequestWrapper>(Channel.UNLIMITED)

    private val requestBuf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)

    val connectionState: Flow<Socket.ConnectionState>
        get() = socket.connectionState

    val events = MutableSharedFlow<Event>()
    private val responses = MutableSharedFlow<LightMessage>()

    private var messageReceiverJob: Job = Job()
    private var requestQueueJob: Job = Job()

    init {
        coroutineScope.launch {
            socket.connectionState.collect {
                if (it == Socket.ConnectionState.Connected) {
                    startRequestQueue()
                    startSocketResponseReceiver()
                } else {
                    // todo: is it required?
                    requestQueueJob.cancel()
                    messageReceiverJob.cancel()
                }
            }
        }
    }

    // todo: don't parse message
    private fun messageToEvent(msg: LightMessage): Event {
        val body = msg.body()
        try {
            val eventId = body.get().toInt()
            if (eventId == 0) {
                val groupId = body.get().toInt()
                if (groupId < PropertyGroup.Id.values().size) {
                    val group = PropertyGroup.Id.values()[groupId]
                    return Event.PropertiesChanged(group)
                } else {
                    Timber.e("Unknown PropertyGroupId: $groupId")
                }
            }
        } catch (ex: BufferUnderflowException) {
            Timber.e("Failed to handle event ${body.toPrettyString(0, body.limit())}")
        }

        return Event.Unknown
    }

    private fun startSocketResponseReceiver() {
        Timber.d("Message receiver started")
        messageReceiverJob = coroutineScope.launch {
            while (isActive) {
                socket.receive()
                    .then { parseMessage(it) }
                    .onSuccess { msg ->
                        coroutineScope.launch { onMessage(msg) }
                    }
                    .onFailure { Timber.e("Failed to receive message: ${it.message}") }
            }
        }
    }

    private suspend fun onMessage(msg: LightMessage) {
        if (msg.isResponse) {
            responses.emit(msg)
        } else if (msg.isEvent) {
            val event = messageToEvent(msg)
            if (event !is Event.Unknown) {
                events.emit(event)
            } else {
                Timber.e("Unknown event")
            }
        }
    }

    private fun parseMessage(data: ByteBuffer): LResult<LightMessage> {
        return try {
            var fnId = data.get().toInt()
            if (fnId < 0) {
                fnId += 256
            }
            val statusId = data.get().toInt()
            val msg = LightMessage(fnId, statusId, data)
            LResult.Success(msg)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Message has no header")
        }
    }

    private fun startRequestQueue() {
        Timber.d("Request queue started")
        requestQueueJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                val request = requestChannel.receive()
                Timber.v("--> running request ${request.data.id}")
                val response = performRequest(request)
                request.onResponse(response)
                Timber.v("<-- finished request ${request.data.id}")
            }
        }
    }

    private suspend fun performRequest(request: RequestWrapper): LResult<ByteBuffer> {
        writeRequest(requestBuf, request.data)

        if (!request.isVoid) {
            sendRequestBuf()
            val response = responses.waitForWithTimeout(RESPONSE_TIMEOUT_MS) { true }
                ?: return LResult.Failure("No response")

            return LResult.Success(response.body())
        } else {
            val result = socket.send(requestBuf)
            return if (result is LResult.Success) {
                LResult.Success(ByteBuffer.allocate(0))
            } else {
                result as LResult.Failure
            }
        }
    }

    private fun sendRequestBuf() {
        coroutineScope.launch {
            socket.send(requestBuf).onFailure {
                Timber.e("Failed to send request: ${it.message}")
            }
        }
    }

    private fun writeRequest(buffer: ByteBuffer, request: Request) {
        requestBuf.clear()
        writeHeader(buffer, request.id)
        writeBody(buffer, request)
    }

    private fun writeHeader(buffer: ByteBuffer, fnId: Int) {
        buffer.put(fnId.toByte())
    }

    private fun writeBody(buffer: ByteBuffer, request: Request) {
        request.writeBody(buffer)
    }

    suspend fun connect(): LResult<Unit> {
        return socket.connect()
    }

    suspend fun disconnect() {
        socket.disconnect()
    }

    suspend fun doRequest(request: Request): LResult<ByteBuffer> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            val requestWrapper = RequestWrapper(
                data = request,
                onResponse = { cont.resume(it) }
            )

            Timber.v("+++ enqueue new request ${request.id}")
            requestChannel.trySend(requestWrapper)
        }
    }

    suspend fun doRequestWithNoResponse(
        request: Request
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val result = suspendCancellableCoroutine { cont ->
            val requestWrapper = RequestWrapper(
                data = request,
                onResponse = { cont.resume(it) },
                isVoid = true
            )

            Timber.v("+++ enqueue new request with no response ${request.id}")
            requestChannel.trySend(requestWrapper)
        }

        if (result is LResult.Success) {
            LResult.Success(Unit)
        } else {
            result as LResult.Failure
        }
    }

    companion object {
        private const val RESPONSE_TIMEOUT_MS = 30000L
    }
}