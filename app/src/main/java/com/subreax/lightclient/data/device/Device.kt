package com.subreax.lightclient.data.device

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface Device {
    enum class State {
        Disconnected, NoConnectivity, Connecting, Fetching, Ready
    }

    val state: StateFlow<State>
    val errors: Flow<LResult.Failure>

    val globalProperties: StateFlow<List<Property>>
    val sceneProperties: StateFlow<List<Property>>

    suspend fun connect(): Flow<State>
    suspend fun disconnect()
    fun findPropertyById(id: Int): Property?
    fun getDeviceDesc(): DeviceDesc
    suspend fun ping(): Int
}