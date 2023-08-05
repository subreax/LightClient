package com.subreax.lightclient.data.device

import android.bluetooth.BluetoothAdapter
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.HciStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

enum class BleConnectionEvent {
    Connecting, Connected, FailedToConnect, Disconnecting, Disconnected, NoConnectivity, HasConnectivity
}

class BleConnectionListener : BluetoothCentralManagerCallback() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _status = MutableSharedFlow<BleConnectionEvent>()
    val status: SharedFlow<BleConnectionEvent>
        get() = _status

    override fun onBluetoothAdapterStateChanged(state: Int) {
        coroutineScope.launch {
            if (state == BluetoothAdapter.STATE_OFF) {
                _status.emit(BleConnectionEvent.NoConnectivity)
            }
            else if (state == BluetoothAdapter.STATE_ON) {
                _status.emit(BleConnectionEvent.HasConnectivity)
            }
        }
    }

    override fun onConnectingPeripheral(peripheral: BluetoothPeripheral) {
        coroutineScope.launch {
            _status.emit(BleConnectionEvent.Connecting)
        }
    }

    override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
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
            _status.emit(BleConnectionEvent.Disconnected)
        }
    }
}
