package com.subreax.lightclient.data.device

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface Device {
    enum class State2 {
        Disconnected, Connecting, Fetching, Ready
    }

    val state: StateFlow<State2>
    val errors: Flow<LResult.Failure>

    val globalProperties: StateFlow<List<Property>>
    val sceneProperties: StateFlow<List<Property>>

    suspend fun connect()
    suspend fun disconnect()
    fun findPropertyById(id: Int): Property?
    fun getDeviceDesc(): DeviceDesc
}