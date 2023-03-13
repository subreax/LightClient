package com.subreax.lightclient.data.controllers

import android.util.Log
import com.subreax.lightclient.data.ConnectivityObserver
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.ApplicationState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectivityController(
    private val appState: ApplicationState,
    private val connectivityObserver: ConnectivityObserver,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val coroutineScope = CoroutineScope(dispatcher)

    fun start() {
        coroutineScope.launch {
            connectivityObserver.status().collect { available ->
                Log.d("ConnectivityController", "available: $available")
                if (available) {
                    appState.notifyEvent(AppEventId.ConnectivityEnabled)
                }
                else {
                    appState.notifyEvent(AppEventId.ConnectivityDisabled)
                }
            }
        }
    }
}