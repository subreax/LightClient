package com.subreax.lightclient.data.device.socket.ble

import android.bluetooth.BluetoothGattCharacteristic
import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.BleConnectionEvent
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.utils.waitForWithTimeout
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

private data class DeviceCharacteristics(
    val request: BluetoothGattCharacteristic,
    val response: BluetoothGattCharacteristic
)

// todo: rename
data class BleDeviceData(
    val peripheral: BluetoothPeripheral,
    val callback: BleDeviceCallback,
    val requestCharacteristic: BluetoothGattCharacteristic
)


class BleConnector(
    private val central: BleCentralContainer,
    private val address: String
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _connectionState = MutableStateFlow(Socket.ConnectionState.Disconnected)
    val connectionState: StateFlow<Socket.ConnectionState>
        get() = _connectionState

    var deviceData: BleDeviceData? = null
        private set

    init {
        coroutineScope.launch {
            central.connectionListener.status.collect {
                if (deviceData != null && it != BleConnectionEvent.Connected) {
                    deviceData = null
                }

                if (it == BleConnectionEvent.NoConnectivity) {
                    _connectionState.value = Socket.ConnectionState.NoConnectivity
                }
                else if (it == BleConnectionEvent.HasConnectivity) {
                    _connectionState.value = Socket.ConnectionState.Disconnected
                }
                // when device disconnected by user
                else if (it == BleConnectionEvent.Disconnected && central.manager.isBluetoothEnabled) {
                    _connectionState.value = Socket.ConnectionState.Disconnected
                }
            }
        }
    }

    suspend fun connect(): LResult<Unit> = withContext(Dispatchers.IO) {
        if (!isBluetoothAvailable()) {
            Timber.e("Failed to connect: bluetooth is off")
            return@withContext LResult.Failure(R.string.bluetooth_is_off)
        }

        _connectionState.value = Socket.ConnectionState.Connecting

        val peripheral = central.manager.getPeripheral(address)
        val callback = BleDeviceCallback()
        val connectResult = tryToConnect(peripheral, callback)
        if (connectResult is LResult.Failure) {
            disconnect(peripheral)
            return@withContext connectResult
        }

        val characteristicsResult = findCharacteristics(peripheral)
        if (characteristicsResult is LResult.Failure) {
            Timber.e("Failed to discover characteristics")
            disconnect()
            return@withContext characteristicsResult
        }

        val characteristics = (characteristicsResult as LResult.Success).value
        peripheral.setNotify(characteristics.response, true)
        delay(200)
        deviceData = BleDeviceData(
            peripheral, callback, characteristics.request
        )

        _connectionState.value = Socket.ConnectionState.Connected
        Timber.d("Connected")
        LResult.Success(Unit)
    }

    private fun isBluetoothAvailable() = central.manager.isBluetoothEnabled

    private suspend fun tryToConnect(
        peripheral: BluetoothPeripheral,
        btPeripheralCallback: BluetoothPeripheralCallback
    ): LResult<Unit> {
        Timber.d("Connecting...")

        if (peripheral.state == ConnectionState.CONNECTED) {
            Timber.d("Already connected")
            return LResult.Success(Unit)
        }

        val res = central.connectionListener.status
            .onSubscription {
                central.manager.connectPeripheral(peripheral, btPeripheralCallback)
            }
            .waitForWithTimeout(3000L) {
                it == BleConnectionEvent.Connected
            }

        // giving the last chance
        if (res == BleConnectionEvent.Connecting) {
            central.connectionListener.status.waitForWithTimeout(500L) {
                it == BleConnectionEvent.Connected
            }
        }

        return if (peripheral.state == ConnectionState.CONNECTED) {
            LResult.Success(Unit)
        } else {
            Timber.e("Failed to connect")
            LResult.Failure(R.string.connection_timed_out)
        }
    }

    private fun findCharacteristics(peripheral: BluetoothPeripheral): LResult<DeviceCharacteristics> {
        Timber.d("Discovering characteristics...")

        val bleService = peripheral.getService(SERVICE_UUID)
            ?: return LResult.Failure(R.string.service_not_found)

        val bleReqCharacteristic =
            bleService.getCharacteristic(REQUEST_CHARACTERISTIC_UUID)
                ?: return LResult.Failure(R.string.request_characteristic_not_found)

        val bleResCharacteristic =
            bleService.getCharacteristic(RESPONSE_CHARACTERISTIC_UUID)
                ?: return LResult.Failure(R.string.response_characteristic_not_found)

        return LResult.Success(
            DeviceCharacteristics(
                bleReqCharacteristic,
                bleResCharacteristic
            )
        )
    }

    suspend fun disconnect() {
        deviceData?.let {
            if (it.peripheral.state != ConnectionState.DISCONNECTED) {
                disconnect(it.peripheral)
            }
            deviceData = null
        }
    }

    private suspend fun disconnect(peripheral: BluetoothPeripheral) = withContext(Dispatchers.IO) {
        peripheral.cancelConnection()
        central.connectionListener.status.waitForWithTimeout(1000L) { event ->
            event == BleConnectionEvent.Disconnected
        }
        _connectionState.value = Socket.ConnectionState.Disconnected
    }

    companion object {
        val SERVICE_UUID = UUID.fromString("2d742143-35c0-4ea0-834e-49968afc1cb9")!!
        val REQUEST_CHARACTERISTIC_UUID = UUID.fromString("1ede3a09-28d5-4085-922f-a5477386932e")!!
        val RESPONSE_CHARACTERISTIC_UUID = UUID.fromString("fef1115b-7f5d-48a4-8ff8-792216cd2da3")!!
    }
}