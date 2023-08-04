package com.subreax.lightclient.data.device

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.welie.blessed.BluetoothCentralManager

class BleCentralContainer(appContext: Context) {
    val connectionListener = BleConnectionListener()
    val manager = BluetoothCentralManager(
        appContext, connectionListener, Handler(Looper.getMainLooper())
    )
}
