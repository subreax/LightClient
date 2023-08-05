package com.subreax.lightclient.data.connection

import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.device.Device
import kotlinx.coroutines.flow.Flow


interface ConnectionRepository {
    val devices: Flow<List<DeviceDesc>>

    suspend fun connect(deviceDesc: DeviceDesc): Flow<Device.State>
    suspend fun disconnect()
}