package com.subreax.lightclient.data.deviceapi.ble

import android.content.Context
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.utils.getUtf8String
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private data class Request(
    val fnId: FunctionId,
    val onWriteBody: ByteBuffer.() -> Unit,
    val onResponse: (LResult<BleResponse>) -> Unit
)

class BleDeviceApi(
    context: Context,
    connectivityObserver: ConnectivityObserver
) : DeviceApi {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val connection = DeviceConnection(context, connectivityObserver)
    private val endpoint: BleDeviceEndpoint?
        get() = connection.endpoint

    private var eventListenerJob: Job? = null

    private val propertyDeserializers = mapOf(
        PropertyType.FloatRange to FloatRangePropertySerializer(),
        PropertyType.Color to ColorPropertySerializer(),
        PropertyType.StringEnum to EnumPropertySerializer()
    )

    override val connectionStatus: Flow<DeviceApi.ConnectionStatus>
        get() = connection.status

    private val _propertiesChanged = MutableSharedFlow<DeviceApi.PropertyGroup>()
    override val propertiesChanged: Flow<DeviceApi.PropertyGroup>
        get() = _propertiesChanged

    private val requestChannel = Channel<Request>(capacity = Channel.UNLIMITED)

    init {
        coroutineScope.launch {
            connection.status.collect {
                if (it == DeviceApi.ConnectionStatus.Connected) {
                    listenForEvents()
                } else {
                    cancelEventListener()
                }
            }
        }

        coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                val request = requestChannel.receive()
                Log.v(TAG, "--> running request ${request.fnId}")

                val response = endpoint?.doRequest(request.fnId, request.onWriteBody)
                    ?: LResult.Failure("No connection")
                request.onResponse(response)
                Log.v(TAG, "<-- finished request ${request.fnId}")
            }
        }
    }

    override suspend fun connect(
        deviceDesc: DeviceDesc
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        return@withContext connection.connect(deviceDesc.address)
    }

    override suspend fun disconnect(): LResult<Unit> = withContext(Dispatchers.IO) {
        connection.disconnect()
        LResult.Success(Unit)
    }

    override fun isConnected() = connection.status.value == DeviceApi.ConnectionStatus.Connected

    override fun getDeviceName() = connection.peripheral?.name ?: "unknown"

    override suspend fun getProperties(
        group: DeviceApi.PropertyGroup,
        progress: MutableStateFlow<Float>
    ): LResult<List<Property>> = withContext(Dispatchers.IO) {
        val propIdsResult = getPropertiesIds(group)
        if (propIdsResult is LResult.Failure) {
            return@withContext propIdsResult
        }

        val ids = (propIdsResult as LResult.Success).value

        val totalRequests = 1 + ids.size*2
        val progressIncr = 1f / totalRequests
        progress.value += progressIncr

        val properties = mutableListOf<Property>()
        for (id in ids) {
            coroutineContext.ensureActive()

            val propertyInfoResult = getPropertyInfoById(id)
            if (propertyInfoResult is LResult.Failure) {
                return@withContext propertyInfoResult
            }

            val property = (propertyInfoResult as LResult.Success).value
            progress.value += progressIncr

            coroutineContext.ensureActive()

            val propertyValueResult = getPropertyValueById(id, property)
            if (propertyValueResult is LResult.Failure) {
                return@withContext propertyValueResult
            }

            properties.add(property)
            progress.value += progressIncr
        }
        LResult.Success(properties)
    }

    private suspend fun getPropertiesIds(group: DeviceApi.PropertyGroup): LResult<Array<Int>> {
        return doRequest(
            FunctionId.GetPropertiesIdsByGroup,
            { put(group.ordinal.toByte()) }
        ) { response ->
            val body = response.body
            val count = body.limit() / 4
            val properties = Array(count) { 0 }
            try {
                for (i in 0 until count) {
                    properties[i] = body.getInt()
                }
                LResult.Success(properties)
            } catch (ex: BufferUnderflowException) {
                LResult.Failure("Failed to read property ids for group $group")
            }
        }
    }

    private suspend fun getPropertyInfoById(id: Int): LResult<Property> {
        return doRequest(FunctionId.GetPropertyInfoById, { putInt(id) }) { response ->
            val buf = response.body
            try {
                buf.position(4) // skip reading id
                val typeInt = buf.get().toInt()
                if (typeInt >= PropertyType.values().size) {
                    return@doRequest LResult.Failure("Unsupported property type: $typeInt")
                }
                val type = PropertyType.values()[typeInt]

                buf.get() // reading groupId
                val name = buf.getUtf8String()

                propertyDeserializers[type]!!.deserializeInfo(id, name, buf)
            } catch (ex: BufferUnderflowException) {
                LResult.Failure("Failed to read property")
            }
        }
    }

    private suspend fun getPropertyValueById(id: Int, target: Property): LResult<Unit> {
        return doRequest(FunctionId.GetPropertyValueById, { putInt(id) }) { response ->
            val deserializer = propertyDeserializers[target.type]!!
            deserializer.deserializeValue(response.body, target)
        }
    }

    override suspend fun setPropertyValue(
        property: Property.FloatRangeProperty,
        value: Float
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val serializer = propertyDeserializers[PropertyType.FloatRange]!!
        endpoint?.doRequestWithoutResponse(FunctionId.SetPropertyValueById) {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }
        delay(1000 / 15)
        LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.ColorProperty,
        value: Int
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val serializer = propertyDeserializers[PropertyType.Color]!!
        endpoint?.doRequestWithoutResponse(FunctionId.SetPropertyValueById) {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }
        delay(1000 / 15)
        LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.ToggleProperty,
        value: Boolean
    ): LResult<Unit> {
        return LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.StringEnumProperty,
        value: Int
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val serializer = propertyDeserializers[PropertyType.StringEnum]!!

        doRequest(FunctionId.SetPropertyValueById, {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }) {
            LResult.Success(Unit)
        }
    }

    private suspend fun <T> doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit,
        onSuccess: (BleResponse) -> LResult<T>
    ): LResult<T> = withContext(Dispatchers.IO) {
        val response = suspendCoroutine { cont ->
            val request = Request(
                fnId = fnId,
                onWriteBody = onWriteBody,
                onResponse = {
                    cont.resume(it)
                }
            )
            Log.v(TAG, "+++ enqueue new request $fnId")
            requestChannel.trySend(request)
        }

        if (response is LResult.Success) {
            onSuccess(response.value)
        } else {
            response as LResult.Failure
        }
    }

    private fun listenForEvents() {
        eventListenerJob = coroutineScope.launch {
            endpoint!!.eventFlow.collect {
                handleNotification(it)
            }
        }
    }

    private fun cancelEventListener() {
        eventListenerJob?.cancel()
        eventListenerJob = null
    }

    private suspend fun handleNotification(notification: BleLightEvent) {
        if (notification is BleLightEvent.PropertiesChanged) {
            Log.v(TAG, "### New event: Properties Changed")
            _propertiesChanged.emit(notification.group)
        }
    }

    companion object {
        private const val TAG = "BleDeviceApi"
    }
}
