package com.subreax.lightclient.data.impl

import com.subreax.lightclient.data.ConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeConnectivityObserver : ConnectivityObserver {
    override fun status(): Flow<Boolean> = flow {
        emit(false)
        delay(1000)
        emit(true)
    }

    override val isAvailable: Boolean
        get() = true
}