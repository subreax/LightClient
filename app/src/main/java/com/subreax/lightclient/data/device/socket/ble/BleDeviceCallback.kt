package com.subreax.lightclient.data.device.socket.ble

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

    override fun onCharacteristicUpdate(
        peripheral: BluetoothPeripheral,
        value: ByteArray?,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
        if (value == null) {
            Log.w(TAG, "${characteristic.uuid}: value is null")
        }

        if (value?.size == 0) {
            Log.w(TAG, "${characteristic.uuid}: empty value")
            return
        }

        val buf = ByteBuffer
            .wrap(value!!)
            .order(ByteOrder.LITTLE_ENDIAN)

        if (characteristic.uuid == BleConnector.RESPONSE_CHARACTERISTIC_UUID) {
            sendPacket(buf)
        }
    }

    private fun sendPacket(buf: ByteBuffer) {
        packetChannel.trySend(buf)
    }

    companion object {
        private const val TAG = "BleDeviceCallback2"
    }
}