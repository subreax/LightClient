package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.utils.getUtf8String
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.HciStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class BleDeviceApi(
    context: Context
) : DeviceApi {
    private val connectionCallback = BtConnectionCallback()
    private val central = BluetoothCentralManager(
        context, connectionCallback, Handler(Looper.getMainLooper())
    )

    private val serviceUuid = UUID.fromString("b7816278-8536-11ed-a1eb-0242ac120002")
    private val rwCharacteristicUuid = UUID.fromString("d4b51c9a-8536-11ed-a1eb-0242ac120002")
    private val notifyCharacteristicUuid = UUID.fromString("b64172a8-8537-11ed-a1eb-0242ac120002")


    private val deviceCallback = BleDeviceCallback(rwCharacteristicUuid, notifyCharacteristicUuid)

    private var device: BleDevice? = null

    private var connectedPeripheral: BluetoothPeripheral? = null
    private var hasDisconnectRequested = AtomicBoolean(false)

    private var bleService: BluetoothGattService? = null
    private var bleRWCharacteristic: BluetoothGattCharacteristic? = null
    private var bleNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionStatus = MutableStateFlow(DeviceApi.ConnectionStatus.Disconnected)
    override val connectionStatus: Flow<DeviceApi.ConnectionStatus>
        get() = _connectionStatus

    private val _propertiesChanged = MutableSharedFlow<DeviceApi.PropertyGroup>()
    override val propertiesChanged: Flow<DeviceApi.PropertyGroup>
        get() = _propertiesChanged

    private val propertyDeserializers = mapOf(
        PropertyType.FloatRange to FloatRangePropertySerializer(),
        PropertyType.Color to ColorPropertySerializer(),
        PropertyType.StringEnum to EnumPropertySerializer()
    )

    init {
        connectionCallback.addListener {
            if (it != DeviceApi.ConnectionStatus.Connected) {
                device = null
                connectedPeripheral = null
                bleService = null
                bleRWCharacteristic = null
                bleNotifyCharacteristic = null
            }
        }
    }

    override suspend fun connect(
        deviceDesc: DeviceDesc
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        val peripheral = central.getPeripheral(deviceDesc.address)

        var status = DeviceApi.ConnectionStatus.Disconnected
        try {
            val s1 = withTimeout(3000) {
                suspendCancellableCoroutine { cont ->
                    connectionCallback.addOneShotListener { status ->
                        cont.resumeWith(Result.success(status))
                    }

                    central.connectPeripheral(peripheral, deviceCallback)
                }
            }

            status = s1
        } catch (ex: TimeoutCancellationException) {
            disconnect()
        }

        if (status == DeviceApi.ConnectionStatus.Connected) {
            connectedPeripheral = peripheral
            bleService = peripheral.getService(serviceUuid)
            if (bleService == null) {
                disconnect()
                return@withContext LResult.Failure("Service not found")
            }

            bleRWCharacteristic = bleService!!.getCharacteristic(rwCharacteristicUuid)
            if (bleRWCharacteristic == null) {
                disconnect()
                return@withContext LResult.Failure("RW Characteristic not found")
            }

            bleNotifyCharacteristic = bleService!!.getCharacteristic(notifyCharacteristicUuid)
            if (bleNotifyCharacteristic == null) {
                disconnect()
                return@withContext LResult.Failure("Notify Characteristic not found")
            }

            connectedPeripheral!!.setNotify(bleNotifyCharacteristic!!, true)
            delay(100)
            connectedPeripheral!!.requestMtu(BluetoothPeripheral.MAX_MTU)
            delay(1000)
            device = BleDevice(connectedPeripheral!!, deviceCallback, bleRWCharacteristic!!)
            LResult.Success(Unit)
        } else {
            LResult.Failure("Failed to connect")
        }
    }

    override suspend fun disconnect(): LResult<Unit> = withContext(Dispatchers.IO) {
        connectedPeripheral?.let { prph ->
            hasDisconnectRequested.set(true)
            suspendCancellableCoroutine { cont ->
                connectionCallback.addOneShotListener { status ->
                    cont.resumeWith(Result.success(Unit))
                }

                central.cancelConnection(prph)
            }
        }

        bleNotifyCharacteristic?.let {
            connectedPeripheral?.setNotify(it, false)
        }

        connectedPeripheral = null
        bleService = null
        bleRWCharacteristic = null
        bleNotifyCharacteristic = null

        LResult.Success(Unit)
    }

    override fun isConnected(): Boolean {
        return _connectionStatus.value == DeviceApi.ConnectionStatus.Connected
    }

    override fun getDeviceName() = connectedPeripheral?.name ?: "unknown"

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
        device?.doRequestWithoutResponse(FunctionId.SetPropertyValueById) {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }
        delay(2 * 16)
        LResult.Success(Unit)
    }

    override suspend fun setPropertyValue(
        property: Property.ColorProperty,
        value: Int
    ): LResult<Unit> {
        return LResult.Success(Unit)
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
    ): LResult<Unit> {
        return LResult.Success(Unit)
    }

    private suspend fun <T> doRequest(
        fnId: FunctionId,
        onWriteBody: ByteBuffer.() -> Unit,
        onSuccess: (BleResponse) -> LResult<T>
    ): LResult<T> = withContext(Dispatchers.IO) {
        val result = device?.doRequest(fnId, onWriteBody) ?: LResult.Failure("No connection")
        if (result is LResult.Success) {
            onSuccess(result.value)
        } else {
            result as LResult.Failure
        }
    }

    private inner class BtConnectionCallback : BluetoothCentralManagerCallback() {
        private val oneShotListeners = mutableListOf<(DeviceApi.ConnectionStatus) -> Unit>()
        private val listeners = mutableListOf<(DeviceApi.ConnectionStatus) -> Unit>()

        fun addOneShotListener(listener: (DeviceApi.ConnectionStatus) -> Unit) {
            oneShotListeners.add(listener)
        }

        fun addListener(listener: (DeviceApi.ConnectionStatus) -> Unit) {
            listeners.add(listener)
        }

        override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
            notify(DeviceApi.ConnectionStatus.Connected)
        }

        override fun onDisconnectedPeripheral(peripheral: BluetoothPeripheral, status: HciStatus) {
            if (hasDisconnectRequested.get()) {
                notify(DeviceApi.ConnectionStatus.Disconnected)
                hasDisconnectRequested.set(false)
            } else {
                notify(DeviceApi.ConnectionStatus.ConnectionLost)
            }
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            notify(DeviceApi.ConnectionStatus.Disconnected)
        }

        private fun notify(status: DeviceApi.ConnectionStatus) {
            _connectionStatus.value = status

            listeners.forEach { listener ->
                listener(status)
            }

            val oneShotListeners1 = oneShotListeners.toList()
            oneShotListeners1.forEach { listener ->
                listener(status)
            }
            oneShotListeners.removeAll(oneShotListeners1)
        }
    }

    companion object {
        private const val TAG = "BleDeviceApi"
    }
}
