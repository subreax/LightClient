package com.subreax.lightclient.data.deviceapi.impl

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.ui.UiText
import com.welie.blessed.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private enum class FunctionId {
    GetPropertyInfoById,
    GetPropertyValueById,
    GetPropertiesIdsByGroup,
    SetPropertyValueById,
}

class BleDeviceApi(
    context: Context
) : DeviceApi {
    private val btCallback = BtCentralManagerCallback()
    private val central = BluetoothCentralManager(context, btCallback, Handler(Looper.getMainLooper()))

    private val serviceUuid = UUID.fromString("b7816278-8536-11ed-a1eb-0242ac120002")
    private val rwCharacteristicUuid = UUID.fromString("d4b51c9a-8536-11ed-a1eb-0242ac120002")
    private val notifyCharacteristicUuid = UUID.fromString("b64172a8-8537-11ed-a1eb-0242ac120002")


    private val peripheralCallback = BlePeripheralCallback2(
        rwCharacteristicUuid, notifyCharacteristicUuid
    )

    private var deviceWrapper: BlePeripheralWrapper? = null

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

    private val userId = System.currentTimeMillis().toInt()

    private val propertyDeserializers = mapOf(
        PropertyType.FloatRange to FloatRangePropertySerializer(),
        PropertyType.Color to ColorPropertySerializer(),
        PropertyType.StringEnum to EnumPropertySerializer()
    )

    init {
        btCallback.addListener {
            if (it != DeviceApi.ConnectionStatus.Connected) {
                deviceWrapper = null
                connectedPeripheral = null
                bleService = null
                bleRWCharacteristic = null
                bleNotifyCharacteristic = null
            }
        }
    }

    override suspend fun connect(device: Device): LResult<Unit> = withContext(Dispatchers.IO) {
        val peripheral = central.getPeripheral(device.address)

        var status = DeviceApi.ConnectionStatus.Disconnected
        try {
            val s1 = withTimeout(3000) {
                suspendCancellableCoroutine { cont ->
                    btCallback.addOneShotListener { status ->
                        connectedPeripheral = peripheral
                        cont.resumeWith(Result.success(status))
                    }

                    central.connectPeripheral(peripheral, peripheralCallback)
                }
            }

            status = s1
        } catch (ex: TimeoutCancellationException) {
            connectedPeripheral = null
            disconnect()
        }

        if (status == DeviceApi.ConnectionStatus.Connected) {
            connectedPeripheral = peripheral
            bleService = peripheral.getService(serviceUuid)
            if (bleService == null) {
                disconnect()
                return@withContext LResult.Failure(UiText.Hardcoded("Service not found"))
            }

            bleRWCharacteristic =
                bleService!!.getCharacteristic(rwCharacteristicUuid)
            if (bleRWCharacteristic == null) {
                disconnect()
                return@withContext LResult.Failure(UiText.Hardcoded("RW Characteristic not found"))
            }

            bleNotifyCharacteristic =
                bleService!!.getCharacteristic(notifyCharacteristicUuid)
            if (bleNotifyCharacteristic == null) {
                disconnect()
                return@withContext LResult.Failure(UiText.Hardcoded("Notify Characteristic not found"))
            }

            connectedPeripheral!!.setNotify(bleNotifyCharacteristic!!, true)
            delay(100)
            connectedPeripheral!!.requestMtu(BluetoothPeripheral.MAX_MTU)
            delay(1000)
            deviceWrapper = BlePeripheralWrapper(connectedPeripheral!!, peripheralCallback, bleRWCharacteristic!!)

            Log.d("BleDeviceApi", "rw properties: ${bleRWCharacteristic!!.properties}")
            Log.d("BleDeviceApi", "notify properties: ${bleNotifyCharacteristic!!.properties}")

            LResult.Success(Unit)
        } else {
            LResult.Failure(UiText.Hardcoded("Failed to connect"))
        }
    }

    override suspend fun disconnect(): LResult<Unit> {
        connectedPeripheral?.let { prph ->
            hasDisconnectRequested.set(true)
            suspendCancellableCoroutine { cont ->
                btCallback.addOneShotListener { status ->
                    cont.resumeWith(Result.success(Unit))
                }

                central.cancelConnection(prph)
            }

            bleNotifyCharacteristic?.let {
                connectedPeripheral?.setNotify(it, false)
            }

            connectedPeripheral = null
            bleService = null
            bleRWCharacteristic = null
            bleNotifyCharacteristic = null
        }
        return LResult.Success(Unit)
    }

    override fun isConnected(): Boolean {
        return _connectionStatus.value == DeviceApi.ConnectionStatus.Connected
    }

    override fun getDeviceName() = connectedPeripheral?.name ?: "unknown"

    override suspend fun getProperties(group: DeviceApi.PropertyGroup): LResult<List<Property>> = withContext(Dispatchers.IO) {
        val result = deviceWrapper?.doRequest(FunctionId.GetPropertiesIdsByGroup) {
            it.put(group.ordinal.toByte())
        } ?: LResult.Failure(UiText.Empty())

        if (result is LResult.Failure) {
            return@withContext result
        }

        val response = (result as LResult.Success).value
        val body = response.body
        val count = body.limit() / 4
        val properties = mutableListOf<Property>()
        for (i in 0 until count) {
            val propId = body.getInt()

            val propertyInfoResult = getPropertyInfoById(propId)
            if (propertyInfoResult is LResult.Failure) {
                return@withContext propertyInfoResult
            }

            val property = (propertyInfoResult as LResult.Success).value

            val propertyValueResult = getPropertyValueById(propId, property)
            if (propertyValueResult is LResult.Failure) {
                return@withContext propertyValueResult
            }

            properties.add(property)
        }
        return@withContext LResult.Success(properties)
    }


    private suspend fun getPropertyInfoById(id: Int): LResult<Property> {
        val result = deviceWrapper?.doRequest(FunctionId.GetPropertyInfoById) {
            it.putInt(id)
        }

        if (result is LResult.Failure) {
            return result
        }

        val buf = (result as LResult.Success).value.body
        try {
            buf.position(4) // skip reading id
            val typeInt = buf.get().toInt()
            if (typeInt >= PropertyType.values().size) {
                return LResult.Failure(UiText.Hardcoded("Unsupported property type: $typeInt"))
            }
            val type = PropertyType.values()[typeInt]

            buf.get() // reading groupId
            val name = buf.getUtf8String()

            return propertyDeserializers[type]!!.deserializeInfo(id, name, buf)
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure(UiText.Hardcoded("Failed to read property"))
        }
    }

    private suspend fun getPropertyValueById(id: Int, target: Property): LResult<Unit> {
        val result = deviceWrapper?.doRequest(FunctionId.GetPropertyValueById) {
            it.putInt(id)
        }
        if (result is LResult.Failure) {
            return result
        }

        val buf = (result as LResult.Success).value.body
        return propertyDeserializers[target.type]!!.deserializeValue(buf, target)
    }

    override suspend fun setPropertyValue(
        property: Property.FloatRangeProperty,
        value: Float
    ): LResult<Unit> = withContext(Dispatchers.IO) {
        deviceWrapper!!.doRequestWithoutResponse(FunctionId.SetPropertyValueById) {
            it.putInt(property.id)
            propertyDeserializers[PropertyType.FloatRange]!!.serializeValue(property, it)
        }
        delay(2*16)
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

    private inner class BtCentralManagerCallback : BluetoothCentralManagerCallback() {
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
            Log.d("DeviceApi", "connection changed: $status")
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

private data class BleResponseHeader(
    val fnId: Byte,
    val userId: Int,
    val status: Byte
) {
    fun isOk(): Boolean = status.toInt() == 0
}


private typealias OnReadListener = (status: GattStatus, data: ByteBuffer) -> Unit
private typealias OnNotificationListener = (data: ByteBuffer) -> Unit

private class BlePeripheralCallback2(
    private val rwCharacteristicUuid: UUID,
    private val notifyCharacteristicUuid: UUID
) : BluetoothPeripheralCallback() {
    private val readListeners = mutableListOf<OnReadListener>()
    private val notificationListeners = mutableListOf<OnNotificationListener>()

    override fun onCharacteristicUpdate(
        peripheral: BluetoothPeripheral,
        value: ByteArray?,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
        if (value == null) {
            Log.w("BleDeviceApi", "characteristic value is null")
        }

        val buf = ByteBuffer.wrap(value!!)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        if (characteristic.uuid == rwCharacteristicUuid) {
            emitOnRead(status, buf)
        }
        else if (characteristic.uuid == notifyCharacteristicUuid) {
            emitOnNotification(buf)
        }
    }


    fun addOnReadListener(listener: OnReadListener): OnReadListener {
        readListeners.add(listener)
        return listener
    }

    fun removeOnReadListener(listener: OnReadListener) {
        readListeners.remove(listener)
    }

    fun addOnNotificationListener(listener: OnNotificationListener): OnNotificationListener {
        notificationListeners.add(listener)
        return listener
    }

    fun removeNotificationListener(listener: OnNotificationListener) {
        notificationListeners.remove(listener)
    }

    private fun emitOnRead(status: GattStatus, buf: ByteBuffer) {
        readListeners.forEach { listener ->
            listener(status, buf)
        }
    }

    private fun emitOnNotification(buf: ByteBuffer) {
        notificationListeners.forEach { listener ->
            listener(buf)
        }
    }
}

private data class BleResponse(
    val header: BleResponseHeader,
    val body: ByteBuffer
)

private class BlePeripheralWrapper(
    private val peripheral: BluetoothPeripheral,
    private val callback: BlePeripheralCallback2,
    private val rwCharacteristic: BluetoothGattCharacteristic
) {
    private val _notificationsFlow = MutableSharedFlow<Unit>()
    val notificationFlow: Flow<Unit>
        get() = _notificationsFlow

    suspend fun doRequest(fnId: FunctionId, onWriteBody: (buf: ByteBuffer) -> Unit): LResult<BleResponse> {
        val buf = ByteBuffer.allocate(512)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.put(fnId.ordinal.toByte())
        buf.putInt(123)
        onWriteBody(buf)

        val header = writeAndWaitNotification(buf) { header ->
            true
        }

        if (header == null) {
            return LResult.Failure(UiText.Hardcoded("No header"))
        }

        val body = read() ?: return LResult.Failure(UiText.Hardcoded("No body"))

        if (header.isOk()) {
            /*Log.d("BleDeviceApi", "response. status: ${header.status}  fnId: ${header.fnId}")
            Log.d("BleDeviceApi", "body: ${body.toPrettyString()}")*/
            return LResult.Success(BleResponse(header, body))
        }
        else {
            return LResult.Failure(UiText.Hardcoded("Failed with status ${header.status}"))
        }
    }

    fun doRequestWithoutResponse(fnId: FunctionId, onWriteBody: (buf: ByteBuffer) -> Unit) {
        val buf = ByteBuffer.allocate(512)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.put(fnId.ordinal.toByte())
        buf.putInt(123)
        onWriteBody(buf)
        writeAsync(buf)
    }

    private fun writeAsync(buf: ByteBuffer) {
        peripheral.writeCharacteristic(rwCharacteristic, buf.getWrittenData(), WriteType.WITHOUT_RESPONSE)
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
            }
            else {
                Log.e("BleDeviceApi", "Failed to read characteristic: $status")
            }
            deferred.complete(Unit)
        }

        readAsync()

        try {
            withTimeout(5000) {
                deferred.await()
            }
        } catch (_: TimeoutCancellationException) {}

        callback.removeOnReadListener(listener)
        return body
    }

    private suspend fun writeAndWaitNotification(buf: ByteBuffer, checkNotification: (BleResponseHeader) -> Boolean): BleResponseHeader? {
        val deferred = CompletableDeferred<Unit>()

        var header: BleResponseHeader? = null
        val listener = callback.addOnNotificationListener {
            header = parseResponseHeader(it)
            if (header == null) {
                Log.e("BleDeviceApi", "Failed to parse response header")
            }
            else if (checkNotification(header!!)) {
                deferred.complete(Unit)
            }
        }


        writeAsync(buf)
        try {
            withTimeout(5000) {
                deferred.await()
            }
        }
        catch (_: TimeoutCancellationException) {}

        callback.removeNotificationListener(listener)
        return header
    }

    private fun parseResponseHeader(data: ByteBuffer): BleResponseHeader? {
        return try {
            val fnId: Byte = data.get()
            val userId: Int = data.getInt()
            val status: Byte = data.get()
            BleResponseHeader(fnId, userId, status)
        }
        catch (ex: BufferUnderflowException) {
            null
        }
    }
}

