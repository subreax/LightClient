package com.subreax.lightclient.data.deviceapi

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface DeviceApi {
    enum class PropertyGroup {
        Global, Scene
    }

    enum class ConnectionStatus {
        Connected, Disconnected, ConnectionLost
    }

    suspend fun connect(deviceDesc: DeviceDesc): LResult<Unit>
    suspend fun disconnect(): LResult<Unit>
    fun isConnected(): Boolean
    fun getDeviceName(): String

    suspend fun getProperties(
        group: PropertyGroup,
        progress: MutableStateFlow<Float>
    ): LResult<List<Property>>

    suspend fun updatePropertyValue(property: Property)

    val connectionStatus: Flow<ConnectionStatus>
    val propertiesChanged: Flow<PropertyGroup>
}
