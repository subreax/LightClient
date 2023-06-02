package com.subreax.lightclient.data.deviceapi.ble

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.utils.waitForWithTimeout
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheral
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*


class DeviceConnection(
    context: Context,
    private val connectivityObserver: ConnectivityObserver
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val connectionListener = BleConnectionListener()
    private val deviceCallback = BleDeviceCallback()
    private val central = BluetoothCentralManager(
        context, connectionListener, Handler(Looper.getMainLooper())
    )

    var peripheral: BluetoothPeripheral? = null
        private set

    var endpoint: BleDeviceEndpoint? = null
        private set

    private var _status = MutableStateFlow(DeviceApi.ConnectionStatus.Disconnected)
    val status: StateFlow<DeviceApi.ConnectionStatus>
        get() = _status


    init {
        coroutineScope.launch {
            connectionListener.status.collect { status ->
                if (status == BleConnectionEvent.ConnectionLost) {
                    disconnectSilently()
                    _status.value = DeviceApi.ConnectionStatus.ConnectionLost
                }
            }
        }
    }

    suspend fun connect(address: String): LResult<Unit> {
        if (!connectivityObserver.isAvailable) {
            return LResult.Failure(R.string.bluetooth_is_off)
        }

        val peripheral = central.getPeripheral(address)

        central.connectPeripheral(peripheral, deviceCallback)
        val res = connectionListener.status.waitForWithTimeout(2000L) {
            it == BleConnectionEvent.Connected
        }

        if (res == null) {
            _status.value = DeviceApi.ConnectionStatus.Disconnected
            disconnectSilently()
            return LResult.Failure(R.string.connection_timed_out)
        }

        val endpointRes = findEndpoints(peripheral)
        if (endpointRes is LResult.Failure) {
            disconnect()
            return endpointRes
        }

        val endpoint = (endpointRes as LResult.Success).value
        endpoint.setEventListener()

        this@DeviceConnection.peripheral = peripheral
        this@DeviceConnection.endpoint = endpoint

        _status.value = DeviceApi.ConnectionStatus.Connected
        return LResult.Success(Unit)
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        disconnectSilently()
        _status.value = DeviceApi.ConnectionStatus.Disconnected
        LResult.Success(Unit)
    }

    /** Disconnects from device without changing connection state **/
    private suspend fun disconnectSilently() {
        endpoint?.cancelEventListener()
        endpoint = null

        peripheral?.let { prph ->
            connectionListener.disconnectAsConnectionLost = false
            central.cancelConnection(prph)
            connectionListener.status.waitForWithTimeout(1000L) {
                it == BleConnectionEvent.Disconnected
            }
            connectionListener.disconnectAsConnectionLost = true
        }

        peripheral = null
    }

    private suspend fun findEndpoints(peripheral: BluetoothPeripheral): LResult<BleDeviceEndpoint> {
        val bleService = peripheral.getService(SERVICE_UUID)
        if (bleService == null) {
            return LResult.Failure("Service not found")
        }

        val bleReqCharacteristic = bleService.getCharacteristic(REQUEST_CHARACTERISTIC_UUID)
        if (bleReqCharacteristic == null) {
            return LResult.Failure("Request Characteristic not found")
        }

        val bleResCharacteristic =
            bleService.getCharacteristic(RESPONSE_CHARACTERISTIC_UUID)
        if (bleResCharacteristic == null) {
            return LResult.Failure("Response Characteristic not found")
        }

        val bleEventCharacteristic =
            bleService.getCharacteristic(EVENT_CHARACTERISTIC_UUID)

        if (bleEventCharacteristic == null) {
            return LResult.Failure("Event Characteristic not found")
        }

        peripheral.setNotify(bleResCharacteristic, true)
        peripheral.setNotify(bleEventCharacteristic, true)
        peripheral.requestMtu(BluetoothPeripheral.MAX_MTU)

        return LResult.Success(
            BleDeviceEndpoint(
                peripheral,
                deviceCallback,
                bleReqCharacteristic
            )
        )
    }

    companion object {
        private const val TAG = "DeviceConnection"
        val SERVICE_UUID = UUID.fromString("b7816278-8536-11ed-a1eb-0242ac120002")!!
        val REQUEST_CHARACTERISTIC_UUID = UUID.fromString("d4b51c9a-8536-11ed-a1eb-0242ac120002")!!
        val RESPONSE_CHARACTERISTIC_UUID = UUID.fromString("b64172a8-8537-11ed-a1eb-0242ac120002")!!
        val EVENT_CHARACTERISTIC_UUID = UUID.fromString("b818f931-5046-4b35-b5c7-eb62e84a2be1")!!
    }
}