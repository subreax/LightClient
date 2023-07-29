package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.GattStatus
import kotlinx.coroutines.channels.Channel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BleDeviceCallback : BluetoothPeripheralCallback() {
    val packetChannel = Channel<ByteBuffer>(Channel.UNLIMITED)
    val eventChannel = Channel<ByteBuffer>(Channel.UNLIMITED)
    var receivePackets = false
    var receiveEvents = false


    override fun onCharacteristicUpdate(
        peripheral: BluetoothPeripheral,
        value: ByteArray?,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
        if (value == null) {
            Log.w(TAG, "${characteristic.uuid}: value is null")
        }

        val buf = ByteBuffer
            .wrap(value!!)
            .order(ByteOrder.LITTLE_ENDIAN)

        when (characteristic.uuid) {
            BleLightConnector.RESPONSE_CHARACTERISTIC_UUID -> {
                if (receivePackets) {
                    packetChannel.trySend(buf)
                }
            }

            BleLightConnector.EVENT_CHARACTERISTIC_UUID -> {
                if (receiveEvents) {
                    eventChannel.trySend(buf)
                }
            }

            else -> {
                Log.w(TAG, "Unknown data")
            }
        }
    }

    companion object {
        private const val TAG = "BleDeviceCallback2"
    }
}