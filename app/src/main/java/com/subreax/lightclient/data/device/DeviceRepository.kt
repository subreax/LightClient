package com.subreax.lightclient.data.device

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.StateFlow


interface DeviceRepository {
    val globalProperties: StateFlow<List<Property>>
    val sceneProperties: StateFlow<List<Property>>

    suspend fun getDeviceName(): String
    fun getPropertyById(id: Int): LResult<Property>
}
