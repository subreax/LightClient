package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.GattStatus
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


typealias OnReadListener = (status: GattStatus, data: ByteBuffer) -> Unit
typealias OnNotificationListener = (data: ByteBuffer) -> Unit

class BleDeviceCallback(
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