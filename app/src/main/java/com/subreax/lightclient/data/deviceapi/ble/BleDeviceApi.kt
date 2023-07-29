package com.subreax.lightclient.data.deviceapi.ble

import android.content.Context
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.deviceapi.LightDevice
import com.subreax.lightclient.utils.getUtf8String
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer


class BleDeviceApi(context: Context) : DeviceApi {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val bleConnector = BleLightConnector(context)
    private var device: LightDevice? = null

    private var eventListenerJob: Job? = null

    private val propertySerializers = mapOf(
        PropertyType.FloatNumber to FloatNumberSerializer(),
        PropertyType.FloatSlider to FloatSliderSerializer(),
        PropertyType.FloatSmallHSlider to FloatSmallHSliderSerializer(),
        PropertyType.Color to ColorPropertySerializer(),
        PropertyType.Enum to EnumPropertySerializer(),
        PropertyType.IntNumber to IntPropertySerializer(),
        PropertyType.IntSlider to IntSliderPropertySerializer(),
        PropertyType.IntSmallHSlider to IntSmallHSliderPropertySerializer(),
        PropertyType.Bool to BoolPropertySerializer()
    )

    private val _connectionStatus = MutableSharedFlow<DeviceApi.ConnectionStatus>()
    override val connectionStatus: Flow<DeviceApi.ConnectionStatus>
        get() = _connectionStatus

    private val _propertiesChanged = MutableSharedFlow<DeviceApi.PropertyGroup>()
    override val propertiesChanged: Flow<DeviceApi.PropertyGroup>
        get() = _propertiesChanged


    override suspend fun connect(
        deviceDesc: DeviceDesc
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val result = bleConnector.connect(deviceDesc)
        if (result is LResult.Success) {
            onConnect(result.value)
            _connectionStatus.emit(DeviceApi.ConnectionStatus.Connected)
            LResult.Success(Unit)
        } else {
            result as LResult.Failure
        }
    }

    private fun onConnect(device: LightDevice) {
        this.device = device
        device.setOnConnectionLostListener {
            Log.d(TAG, "Connection lost")
            onDisconnect()
            coroutineScope.launch {
                _connectionStatus.emit(DeviceApi.ConnectionStatus.ConnectionLost)
            }
        }
        startEventListener()
    }

    override suspend fun disconnect(): LResult<Unit> = withContext(Dispatchers.IO) {

        device?.disconnect()
        onDisconnect()
        _connectionStatus.emit(DeviceApi.ConnectionStatus.Disconnected)
        LResult.Success(Unit)
    }

    private fun onDisconnect() {
        device = null
        cancelEventListener()
    }

    override fun isConnected() = device != null

    override fun getDeviceName() = device?.name ?: "Disconnected"

    override suspend fun getProperties(
        group: DeviceApi.PropertyGroup,
        progress: MutableStateFlow<Float>
    ): LResult<List<Property>> = withContext(Dispatchers.IO) {
        doRequest(FunctionId.GetPropertiesFromGroup, { put(group.ordinal.toByte()) }) { response ->
            val props = mutableListOf<Property>()
            val body = response
            while (body.position() < body.limit()) {
                val sz = body.getShort().toInt()
                val oldLimit = body.limit()
                body.limit(body.position() + sz)
                val propertyResult = parsePropertyInfo(body)
                if (propertyResult is LResult.Failure) {
                    return@doRequest propertyResult
                }

                val property = (propertyResult as LResult.Success).value
                val deserializer = propertySerializers[property.type]!!
                val parseValueResult = deserializer.deserializeValue(response, property)
                if (parseValueResult is LResult.Failure) {
                    return@doRequest parseValueResult
                }
                body.limit(oldLimit)
                props.add(property)
            }

            LResult.Success(props)
        }
    }

    private suspend fun getPropertiesIds(group: DeviceApi.PropertyGroup): LResult<Array<Int>> {
        return doRequest(
            FunctionId.GetPropertiesIdsByGroup,
            { put(group.ordinal.toByte()) }
        ) { response ->
            val body = response
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
            try {
                parsePropertyInfo(id, response)
            } catch (ex: BufferUnderflowException) {
                LResult.Failure("Failed to read property")
            }
        }
    }

    private fun parsePropertyInfo(id: Int, buf: ByteBuffer): LResult<Property> {
        buf.position(4) // skip reading id
        val typeInt = buf.get().toInt()
        if (typeInt >= PropertyType.values().size) {
            return LResult.Failure("Unsupported property type: $typeInt")
        }
        val type = PropertyType.values()[typeInt]

        buf.get() // reading groupId
        val name = buf.getUtf8String()

        return propertySerializers[type]!!.deserializeInfo(id, name, buf)
    }

    private fun parsePropertyInfo(buf: ByteBuffer): LResult<Property> {
        val id = buf.getInt()
        val typeInt = buf.get().toInt()
        if (typeInt >= PropertyType.values().size) {
            return LResult.Failure("Unsupported property type: $typeInt")
        }
        val type = PropertyType.values()[typeInt]

        buf.get() // reading groupId
        val name = buf.getUtf8String()

        return propertySerializers[type]!!.deserializeInfo(id, name, buf)
    }

    private suspend fun getPropertyValueById(id: Int, target: Property): LResult<Unit> {
        return doRequest(FunctionId.GetPropertyValueById, { putInt(id) }) { res ->
            val deserializer = propertySerializers[target.type]!!
            deserializer.deserializeValue(res, target)
        }
    }

    private suspend fun getPropertyById(id: Int): LResult<Property> {
        return doRequest(FunctionId.GetPropertyById, { putInt(id) }) { res ->
            val result = parsePropertyInfo(id, res)
            if (result is LResult.Failure) {
                return@doRequest result
            }

            val property = (result as LResult.Success).value

            val serializer = propertySerializers[property.type]!!
            val result2 = serializer.deserializeValue(res, property)
            if (result2 is LResult.Success) {
                LResult.Success(property)
            } else {
                result2 as LResult.Failure
            }
        }
    }

    override suspend fun updatePropertyValue(property: Property) = withContext(Dispatchers.IO) {
        val serializer = propertySerializers[property.type]
        if (serializer == null) {
            Log.e(TAG, "No serializer found for property type ${property.type}")
            return@withContext
        }

        //updatePropertyValueSync(serializer, property)

        if (property.type == PropertyType.Enum) {
            updatePropertyValueSync(serializer, property)
        } else {
            updatePropertyValueAsync(serializer, property)
        }
    }

    private suspend fun updatePropertyValueSync(
        serializer: PropertySerializer,
        property: Property
    ) {
        doRequest(
            fnId = FunctionId.SetPropertyValueById,
            onWriteBody = {
                putInt(property.id)
                serializer.serializeValue(property, this)
            },
            onSuccess = { LResult.Success(Unit) }
        )
    }

    private suspend fun updatePropertyValueAsync(
        serializer: PropertySerializer,
        property: Property
    ) {
        doRequestWithNoResponse(FunctionId.SetPropertyValueById) {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }
        delay(50)
    }

    private suspend fun <T> _doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit,
        exec: suspend LightDevice.(LightDevice.Request) -> LResult<T>
    ): LResult<T> = withContext(Dispatchers.IO) {
        val request = LightDevice.Request(fnId, onWriteBody)
        val notNullDevice = device ?: return@withContext LResult.Failure("Disconnected")
        exec(notNullDevice, request)
    }

    private suspend fun <T> doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit,
        onSuccess: (ByteBuffer) -> LResult<T>
    ): LResult<T> = withContext(Dispatchers.IO) {
        _doRequest(
            fnId = fnId,
            onWriteBody = onWriteBody,
            exec = { req ->
                val response = doRequest(req)
                if (response is LResult.Success) {
                    onSuccess(response.value)
                } else {
                    response as LResult.Failure
                }
            }
        )
    }

    private suspend fun doRequestWithNoResponse(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        _doRequest(
            fnId = fnId,
            onWriteBody = onWriteBody,
            exec = { req ->
                doRequestWithNoResponse(req)
                LResult.Success(Unit)
            }
        )
    }

    private fun startEventListener() {
        device?.let {
            eventListenerJob = coroutineScope.launch {
                it.events.collect {
                    handleEvent(it)
                }
            }
        }
    }

    private fun cancelEventListener() {
        eventListenerJob?.cancel()
        eventListenerJob = null
    }

    private suspend fun handleEvent(notification: BleLightEvent) {
        if (notification is BleLightEvent.PropertiesChanged) {
            _propertiesChanged.emit(notification.group)
        }
    }

    companion object {
        private const val TAG = "BleDeviceApi"
    }
}
