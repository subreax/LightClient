package com.subreax.lightclient.data

import kotlinx.coroutines.flow.Flow


interface DeviceRepository {
    val globalProperties: Flow<List<Property>>
    val sceneProperties: Flow<List<Property>>

    suspend fun getDeviceName(): String
    fun setPropertyValue(property: Property.ToggleProperty, value: Boolean)
    fun setPropertyValue(property: Property.FloatRangeProperty, value: Float)
}