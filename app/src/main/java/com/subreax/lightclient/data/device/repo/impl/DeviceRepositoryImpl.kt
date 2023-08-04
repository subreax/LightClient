package com.subreax.lightclient.data.device.repo.impl

import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.data.device.impl.DeviceImpl
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.socket.ble.BleSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    private val centralContainer: BleCentralContainer
) : DeviceRepository {
    private var _device: Device? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun connect(deviceDesc: DeviceDesc) = flow {
        val device = createDevice(deviceDesc)

        device.state
            .onSubscription {
                coroutineScope.launch { device.connect() }
            }
            .drop(1)
            .takeWhile {
                listOf(
                    Device.State2.Connecting,
                    Device.State2.Fetching
                ).contains(it)
            }
            .collect {
                emit(it)
            }

        if (device.state.value == Device.State2.Ready) {
            _device = device
        }

        emit(device.state.value)
    }

    private fun createDevice(deviceDesc: DeviceDesc): Device {
        return DeviceImpl(deviceDesc, createSocket(deviceDesc))
    }

    private fun createSocket(deviceDesc: DeviceDesc): Socket {
        return BleSocket(centralContainer, deviceDesc.address)
    }

    override fun getDevice(): Device = _device!!

    override fun isConnected(): Boolean {
        return _device?.let {
            it.state.value == Device.State2.Ready
        } ?: false
    }

    companion object {
        private const val TAG = "DeviceRepositoryImpl"
    }
}