package com.subreax.lightclient.data.state.controllers

import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiLog
import kotlinx.coroutines.*
import javax.inject.Inject

class ReconnectionController @Inject constructor(
    private val appState: ApplicationState,
    private val connectionRepository: ConnectionRepository,
    private val uiLog: UiLog,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val coroutineScope = CoroutineScope(dispatcher)

    fun start() {
        coroutineScope.launch {
            appState.stateId.collect {
                when (it) {
                    AppStateId.Reconnecting -> {
                        tryToConnect()
                    }

                    else -> {  }
                }
            }
        }
    }

    private suspend fun tryToConnect() {
        var success = false
        var result: LResult<Unit> = LResult.Success(Unit)
        for (i in 0 until RECONNECT_ATTEMPTS_COUNT) {
            Log.d(TAG, "Connecting... Attempt #$i")
            result = connectionRepository.connect()
            if (result is LResult.Success) {
                success = true
                break
            }
            else if (i+1 != RECONNECT_ATTEMPTS_COUNT) {
                delay(RECONNECT_DELAY_MS)
            }
        }

        if (success) {
            Log.d(TAG, "Success")
        }
        else {
            appState.notifyEvent(AppEventId.Disconnected)
            uiLog.e((result as LResult.Failure).message)
            Log.d(TAG, "Failed to connect")
        }
    }

    companion object {
        private const val TAG = "ConnectionController"
        private const val RECONNECT_ATTEMPTS_COUNT = 10
        private const val RECONNECT_DELAY_MS = 3000L
    }
}