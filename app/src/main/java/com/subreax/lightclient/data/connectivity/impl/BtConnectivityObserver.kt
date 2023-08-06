package com.subreax.lightclient.data.connectivity.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber

class BtConnectivityObserver(appContext: Context) : ConnectivityObserver {
    private val _status = MutableSharedFlow<ConnectivityObserver.Event>(extraBufferCapacity = 3)
    override val status: Flow<ConnectivityObserver.Event>
        get() = _status

    private val _btBroadcastReceiver = BtBroadcastReceiver(_status)

    init {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        val receiverFlags = ContextCompat.RECEIVER_EXPORTED
        ContextCompat.registerReceiver(appContext, _btBroadcastReceiver, filter, receiverFlags)
    }

    private class BtBroadcastReceiver(
        private val status: MutableSharedFlow<ConnectivityObserver.Event>
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }

            Timber.d("action: ${intent.action}")
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    handleAdapterStateChanged(intent)
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    status.tryEmit(ConnectivityObserver.Event.Connected)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    status.tryEmit(ConnectivityObserver.Event.Disconnected)
                }
            }
        }

        private fun handleAdapterStateChanged(intent: Intent) {
            val btState = intent.extras?.getInt(BluetoothAdapter.EXTRA_STATE, -1)
            if (btState == BluetoothAdapter.STATE_ON) {
                status.tryEmit(ConnectivityObserver.Event.Available)
            }
            else if (btState == BluetoothAdapter.STATE_OFF) {
                status.tryEmit(ConnectivityObserver.Event.NotAvailable)
            }
        }
    }
}