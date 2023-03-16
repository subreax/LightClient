package com.subreax.lightclient.data.connectivity.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class BtConnectivityObserver(appContext: Context) : ConnectivityObserver {
    private val _status = MutableStateFlow(false)
    private val _btBroadcastReceiver = BtBroadcastReceiver(_status)

    override fun status(): Flow<Boolean> = _status

    override val isAvailable: Boolean
        get() = _status.value

    init {
        val btManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        _status.value = btManager.adapter?.isEnabled ?: false

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val receiverFlags = ContextCompat.RECEIVER_EXPORTED
        ContextCompat.registerReceiver(appContext, _btBroadcastReceiver, filter, receiverFlags)
    }

    private class BtBroadcastReceiver(private val status: MutableStateFlow<Boolean>) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }

            val state = intent.extras?.getInt(BluetoothAdapter.EXTRA_STATE) ?: BluetoothAdapter.STATE_OFF
            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    status.value = true
                }

                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_OFF,
                BluetoothAdapter.STATE_TURNING_ON -> {
                    status.value = false
                }
            }
        }
    }
}