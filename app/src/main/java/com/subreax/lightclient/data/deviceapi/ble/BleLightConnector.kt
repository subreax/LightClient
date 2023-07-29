package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.deviceapi.LightDevice
import com.subreax.lightclient.utils.waitForWithTimeout
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.ConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription
import java.util.UUID

private data class DeviceCharacteristics(
    val request: BluetoothGattCharacteristic,
    val response: BluetoothGattCharacteristic,
    val event: BluetoothGattCharacteristic
)

class BleLightConnector(appContext: Context) {
    private val connectionListener = BleConnectionListener()
    private val central = BluetoothCentralManager(
        appContext, connectionListener, Handler(Looper.getMainLooper())
    )

    suspend fun connect(deviceDesc: DeviceDesc): LResult<LightDevice> {
        if (!isBluetoothAvailable()) {
            return LResult.Failure(R.string.bluetooth_is_off)
        }

        val peripheral = central.getPeripheral(deviceDesc.address)
        val callback = BleDeviceCallback()
        val connectResult = tryToConnect(peripheral, callback)
        if (connectResult is LResult.Failure) {
            peripheral.cancelConnection()
            return connectResult
        }

        val characteristicsResult = findCharacteristics(peripheral)
        if (characteristicsResult is LResult.Failure) {
            peripheral.cancelConnection()
            return characteristicsResult
        }

        val characteristics = (characteristicsResult as LResult.Success).value
        peripheral.setNotify(characteristics.response, true)
        peripheral.setNotify(characteristics.event, true)
        peripheral.requestMtu(BluetoothPeripheral.MAX_MTU)
        delay(500)
        val device = BleLightDevice(peripheral, callback, characteristics.request)
        return LResult.Success(device)
    }

    private fun isBluetoothAvailable() = central.isBluetoothEnabled

    private suspend fun tryToConnect(
        peripheral: BluetoothPeripheral,
        btPeripheralCallback: BluetoothPeripheralCallback
    ): LResult<Unit> {
        if (!peripheral.isConnected()) {
            val res = connectionListener.status
                .onSubscription {
                    central.connectPeripheral(peripheral, btPeripheralCallback)
                }
                .waitForWithTimeout(3000L) {
                    it == BleConnectionEvent.Connected
                }

            // giving the last chance
            if (res == BleConnectionEvent.Connecting) {
                connectionListener.status.waitForWithTimeout(500L) {
                    it == BleConnectionEvent.Connected
                }
            }

            if (peripheral.state != ConnectionState.CONNECTED) {
                return LResult.Failure(R.string.connection_timed_out)
            }
        }

        return LResult.Success(Unit)
    }

    private fun findCharacteristics(peripheral: BluetoothPeripheral): LResult<DeviceCharacteristics> {
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

        return LResult.Success(
            DeviceCharacteristics(
                bleReqCharacteristic,
                bleResCharacteristic,
                bleEventCharacteristic
            )
        )
    }

    private fun BluetoothPeripheral.isConnected() = state == ConnectionState.CONNECTED

    companion object {
        private const val TAG = "BleLightConnector"

        val SERVICE_UUID = UUID.fromString("b7816278-8536-11ed-a1eb-0242ac120002")!!
        val REQUEST_CHARACTERISTIC_UUID = UUID.fromString("d4b51c9a-8536-11ed-a1eb-0242ac120002")!!
        val RESPONSE_CHARACTERISTIC_UUID = UUID.fromString("b64172a8-8537-11ed-a1eb-0242ac120002")!!
        val EVENT_CHARACTERISTIC_UUID = UUID.fromString("b818f931-5046-4b35-b5c7-eb62e84a2be1")!!
    }
}