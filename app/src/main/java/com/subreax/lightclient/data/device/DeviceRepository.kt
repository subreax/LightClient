package com.subreax.lightclient.data.device

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.StateFlow


interface DeviceRepository {
    val globalProperties: StateFlow<List<Property>>
    val sceneProperties: StateFlow<List<Property>>

    suspend fun getDeviceName(): String
    fun getPropertyById(id: Int): LResult<Property>
    fun setPropertyValue(property: Property.Bool, value: Boolean)
    fun setPropertyValue(property: Property.FloatSlider, value: Float)
    fun setPropertyValue(property: Property.IntNumber, value: Int)
    fun setPropertyValue(property: Property.IntSlider, value: Int)
    fun setPropertyValue(property: Property.Color, value: Int)
    fun setPropertyValue(property: Property.Enum, value: Int)
}
