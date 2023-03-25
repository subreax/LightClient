package com.subreax.lightclient.data.deviceapi

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.Flow

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

    suspend fun getProperties(group: PropertyGroup): LResult<List<Property>>
    suspend fun setPropertyValue(property: Property.FloatRangeProperty, value: Float): LResult<Unit>
    suspend fun setPropertyValue(property: Property.ColorProperty, value: Int): LResult<Unit>
    suspend fun setPropertyValue(property: Property.ToggleProperty, value: Boolean): LResult<Unit>
    suspend fun setPropertyValue(property: Property.StringEnumProperty, value: Int): LResult<Unit>

    val connectionStatus: Flow<ConnectionStatus>
    val propertiesChanged: Flow<PropertyGroup>
}
