package com.subreax.lightclient.data.deviceapi.ble

import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.HciStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

enum class BleConnectionEvent {
    Connecting, Connected, FailedToConnect, Disconnecting, Disconnected, ConnectionLost
}

class BleConnectionListener : BluetoothCentralManagerCallback() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _status = MutableSharedFlow<BleConnectionEvent>()
    val status: Flow<BleConnectionEvent>
        get() = _status

    var disconnectAsConnectionLost = true

    override fun onConnectingPeripheral(peripheral: BluetoothPeripheral) {
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Connecting)
        }
    }

    override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
        disconnectAsConnectionLost = true
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Connected)
        }
    }

    override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.FailedToConnect)
        }
    }

    override fun onDisconnectingPeripheral(peripheral: BluetoothPeripheral) {
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Disconnecting)
        }
    }

    override fun onDisconnectedPeripheral(peripheral: BluetoothPeripheral, status: HciStatus) {
        coroutineScope.launch {
            if (disconnectAsConnectionLost) {
                _status.emit(BleConnectionEvent.ConnectionLost)
                disconnectAsConnectionLost = true
            }
            else {
                _status.emit(BleConnectionEvent.Disconnected)
            }
        }
    }
}
