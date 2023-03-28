package com.subreax.lightclient.data.device

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.Flow


interface DeviceRepository {
    val globalProperties: Flow<List<Property>>
    val sceneProperties: Flow<List<Property>>

    suspend fun getDeviceName(): String
    fun getPropertyById(id: Int): LResult<Property>
    fun setPropertyValue(property: Property.ToggleProperty, value: Boolean)
    fun setPropertyValue(property: Property.FloatRangeProperty, value: Float)
    fun setPropertyValue(property: Property.ColorProperty, value: Int)
}