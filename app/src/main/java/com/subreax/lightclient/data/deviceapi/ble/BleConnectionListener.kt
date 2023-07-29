package com.subreax.lightclient.data.deviceapi.ble

import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.HciStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

enum class BleConnectionEvent {
    Connecting, Connected, FailedToConnect, Disconnecting, Disconnected, ConnectionLost
}

class BleConnectionListener : BluetoothCentralManagerCallback() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _status = MutableSharedFlow<BleConnectionEvent>()
    val status: SharedFlow<BleConnectionEvent>
        get() = _status

    //var emitConnectionLostWhenDisconnected = true

    override fun onConnectingPeripheral(peripheral: BluetoothPeripheral) {
        //Log.d(TAG, "onConnectingPeripheral")
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Connecting)
        }
    }

    override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
        //Log.d(TAG, "onConnectedPeripheral")
        //emitConnectionLostWhenDisconnected = true
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Connected)
        }
    }

    override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
        //Log.d(TAG, "onConnectionFailed")
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.FailedToConnect)
        }
    }

    override fun onDisconnectingPeripheral(peripheral: BluetoothPeripheral) {
        //Log.d(TAG, "onDisconnectingPeripheral")
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Disconnecting)
        }
    }

    override fun onDisconnectedPeripheral(peripheral: BluetoothPeripheral, status: HciStatus) {
        //Log.d(TAG, "onDisconnectedPeripheral")
        coroutineScope.launch {
            //if (emitConnectionLostWhenDisconnected) {
            //    _status.emit(BleConnectionEvent.ConnectionLost)
            //    emitConnectionLostWhenDisconnected = true
            //}
            //else {
            _status.emit(BleConnectionEvent.Disconnected)
            //}
        }
    }

    companion object {
        private const val TAG = "BleConnectionListener"
    }
}
