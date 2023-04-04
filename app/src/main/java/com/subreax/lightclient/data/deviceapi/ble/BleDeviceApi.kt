package com.subreax.lightclient.data.deviceapi.ble

import android.content.Context
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.utils.getUtf8String
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer


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
        group: DeviceApi.PropertyGroup
    ): LResult<List<Property>> = withContext(Dispatchers.IO) {
        val propIdsResult = getPropertiesIds(group)
        if (propIdsResult is LResult.Failure) {
            return@withContext propIdsResult
        }

        val ids = (propIdsResult as LResult.Success).value
        val properties = mutableListOf<Property>()
        ids.forEach { id ->
            val propertyInfoResult = getPropertyInfoById(id)
            if (propertyInfoResult is LResult.Failure) {
                return@withContext propertyInfoResult
            }

            val property = (propertyInfoResult as LResult.Success).value

            val propertyValueResult = getPropertyValueById(id, property)
            if (propertyValueResult is LResult.Failure) {
                return@withContext propertyValueResult
            }

            properties.add(property)
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
        endpoint?.doRequestWithoutResponse(FunctionId.SetPropertyValueById) {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }
        delay(1000 / 15)
        LResult.Success(Unit)
    }

    private suspend fun <T> doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit,
        onSuccess: (BleResponse) -> LResult<T>
    ): LResult<T> = withContext(Dispatchers.IO) {
        val result = endpoint?.doRequest(fnId, onWriteBody) ?: LResult.Failure("No connection")
        if (result is LResult.Success) {
            onSuccess(result.value)
        } else {
            result as LResult.Failure
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
            _propertiesChanged.emit(notification.group)
        }
    }

    companion object {
        private const val TAG = "BleDeviceApi"
    }
}
