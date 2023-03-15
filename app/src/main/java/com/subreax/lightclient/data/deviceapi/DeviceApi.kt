package com.subreax.lightclient.data.deviceapi

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.Flow

interface DeviceApi {
    enum class PropertyGroup {
        Global, Scene
    }

    suspend fun connect(device: Device): LResult<Unit>
    suspend fun disconnect(): LResult<Unit>
    fun isConnected(): Boolean
    fun getDeviceName(): String

    suspend fun getProperties(group: PropertyGroup): LResult<List<Property>>
    suspend fun setPropertyValue(property: Property.FloatRangeProperty, value: Float): LResult<Unit>
    suspend fun setPropertyValue(property: Property.ColorProperty, value: Long): LResult<Unit>
    suspend fun setPropertyValue(property: Property.ToggleProperty, value: Boolean): LResult<Unit>
    suspend fun setPropertyValue(property: Property.StringEnumProperty, value: Int): LResult<Unit>

    val connectionStatus: Flow<Boolean>
    val propertiesChanged: Flow<PropertyGroup>
}
