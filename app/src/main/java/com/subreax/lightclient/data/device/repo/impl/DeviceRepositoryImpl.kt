package com.subreax.lightclient.data.device.repo.impl

import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.data.device.impl.DeviceImpl
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.data.device.socket.ReconnectionSocket
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.data.device.socket.ble.BleSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    private val centralContainer: BleCentralContainer
) : DeviceRepository {
    private var _device: Device? = null

    override suspend fun connect(deviceDesc: DeviceDesc): Flow<Device.State> {
        val device = createDevice(deviceDesc)
        return device.connect().onCompletion {
            if (device.state.value == Device.State.Ready) {
                _device = device
            }
        }
    }

    private fun createDevice(deviceDesc: DeviceDesc): Device {
        return DeviceImpl(deviceDesc, createSocket(deviceDesc))
    }

    private fun createSocket(deviceDesc: DeviceDesc): Socket {
        return ReconnectionSocket(
            BleSocket(centralContainer, deviceDesc.address)
        )
    }

    override fun getDevice(): Device = _device!!

    override fun isConnected(): Boolean {
        return _device?.let {
            it.state.value == Device.State.Ready
        } ?: false
    }
}