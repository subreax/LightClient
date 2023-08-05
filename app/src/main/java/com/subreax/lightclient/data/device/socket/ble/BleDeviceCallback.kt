package com.subreax.lightclient.data.device.socket.ble

import android.bluetooth.BluetoothGattCharacteristic
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.GattStatus
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
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
            Timber.w("${characteristic.uuid}: value is null")
        }

        if (value?.size == 0) {
            Timber.w("${characteristic.uuid}: empty value")
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
}