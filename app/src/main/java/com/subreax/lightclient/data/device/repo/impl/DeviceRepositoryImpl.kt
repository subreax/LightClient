package com.subreax.lightclient.data.device.repo.impl

import android.content.Context
import com.subreax.lightclient.data.ConnectionType
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connectivity.impl.BtConnectivityObserver
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.data.device.api.bin.BinDeviceApi
import com.subreax.lightclient.data.device.api.old.OldDeviceApi
import com.subreax.lightclient.data.device.impl.DeviceImpl
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.data.device.socket.ReconnectionSocket
import com.subreax.lightclient.data.device.socket.ble.BleSocket
import com.subreax.lightclient.data.device.socket.bt.OldBtSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    private val centralContainer: BleCentralContainer,
    private val context: Context
) : DeviceRepository {
    private var _device: Device? = null
    private val btConnectivityObserver by lazy {
        BtConnectivityObserver(context)
    }

    override suspend fun connect(deviceDesc: DeviceDesc): Flow<Device.State> {
        val device = createDevice(deviceDesc)
        return device.connect().onCompletion {
            if (device.state.value == Device.State.Ready) {
                _device = device
            }
        }
    }

    private fun createDevice(deviceDesc: DeviceDesc): Device {
        return if (deviceDesc.connectionType == ConnectionType.BLE) {
            BleDevice(centralContainer, deviceDesc)
        } else {
            OldBtDevice(context, deviceDesc, btConnectivityObserver)
        }
    }

    override fun getDevice(): Device = _device!!

    override fun isConnected(): Boolean {
        return _device?.let {
            it.state.value == Device.State.Ready
        } ?: false
    }
}

private fun BleDevice(
    centralContainer: BleCentralContainer,
    deviceDesc: DeviceDesc
): Device {
    val socket = ReconnectionSocket(BleSocket(centralContainer, deviceDesc.address))
    val api = BinDeviceApi(socket)
    return DeviceImpl(deviceDesc, socket, api)
}

private fun OldBtDevice(
    context: Context,
    deviceDesc: DeviceDesc,
    connObserver: BtConnectivityObserver,
): Device {
    val socket = ReconnectionSocket(OldBtSocket(context, connObserver, deviceDesc.address))
    val api = OldDeviceApi(socket)
    return DeviceImpl(deviceDesc, socket, api)
}