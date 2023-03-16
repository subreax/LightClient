package com.subreax.lightclient.data.connectivity.impl

import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FakeConnectivityObserver : ConnectivityObserver {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        coroutineScope.launch {
            _status.value = false
            delay(1000)

            _status.value = true
        }
    }

    private val _status = MutableStateFlow(false)
    override fun status(): Flow<Boolean> = _status

    override val isAvailable: Boolean
        get() = _status.value
}