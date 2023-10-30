package com.subreax.lightclient.data.device.api

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.PropertyGroup
import kotlinx.coroutines.flow.Flow

interface DeviceApi {
    val events: Flow<Event>

    suspend fun getPropertiesFromGroup(group: PropertyGroup.Id): LResult<List<Property>>
    suspend fun uploadPropertyValue(property: Property): LResult<Unit>
    suspend fun ping(): LResult<Unit>
}
