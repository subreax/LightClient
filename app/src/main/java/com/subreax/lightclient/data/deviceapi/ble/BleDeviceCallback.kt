package com.subreax.lightclient.data.deviceapi.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.GattStatus
import java.nio.ByteBuffer
import java.nio.ByteOrder


typealias OnResponseListener = (data: ByteBuffer) -> Unit
typealias OnEventListener = (data: ByteBuffer) -> Unit

class BleDeviceCallback : BluetoothPeripheralCallback() {
    private val responseListeners = mutableListOf<OnResponseListener>()
    private val eventListeners = mutableListOf<OnEventListener>()

    override fun onCharacteristicUpdate(
        peripheral: BluetoothPeripheral,
        value: ByteArray?,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
        if (value == null) {
            Log.w(TAG, "${characteristic.uuid}: value is null")
        }

        val buf = ByteBuffer.wrap(value!!)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        when (characteristic.uuid) {
            DeviceConnection.RESPONSE_CHARACTERISTIC_UUID -> {
                emitOnResponse(buf)
            }
            DeviceConnection.EVENT_CHARACTERISTIC_UUID -> {
                emitOnEvent(buf)
            }
        }
    }

    fun addOnPacketListener(listener: OnResponseListener): OnResponseListener {
        responseListeners.add(listener)
        return listener
    }

    fun removeResponseListener(listener: OnResponseListener) {
        responseListeners.remove(listener)
    }

    fun addEventListener(listener: OnEventListener): OnEventListener {
        eventListeners.add(listener)
        return listener
    }

    fun removeEventListener(listener: OnEventListener) {
        eventListeners.remove(listener)
    }

    private fun emitOnResponse(buf: ByteBuffer) {
        responseListeners.forEach { listener ->
            listener(buf)
        }
    }

    private fun emitOnEvent(buf: ByteBuffer) {
        eventListeners.forEach { listener ->
            listener(buf)
        }
    }

    companion object {
        private const val TAG = "BleDeviceCallback"
    }
}