package com.subreax.lightclient.data.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun status(): Flow<Boolean>

    val isAvailable: Boolean
}