package com.subreax.lightclient.data.device.repo

import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.device.Device
import kotlinx.coroutines.flow.Flow


interface DeviceRepository {
    suspend fun connect(deviceDesc: DeviceDesc): Flow<Device.State>
    fun getDevice(): Device
    fun isConnected(): Boolean
}
