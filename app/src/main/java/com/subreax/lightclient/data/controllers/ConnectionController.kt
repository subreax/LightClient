package com.subreax.lightclient.data.controllers

import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.ConnectionRepository
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiLog
import kotlinx.coroutines.*
import javax.inject.Inject

class ConnectionController @Inject constructor(
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
                    AppStateId.Connecting -> {
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
        for (i in 1..3) {
            Log.d(TAG, "Connecting... Attempt #$i")
            result = connectionRepository.connect()
            if (result is LResult.Success) {
                success = true
                break
            }
            else {
                //delay(2000L)
            }
        }

        if (success) {
            Log.d(TAG, "Success")
            appState.notifyEvent(AppEventId.Connected)
        }
        else {
            connectionRepository.disconnect()
            uiLog.e((result as LResult.Failure).message)
            Log.d(TAG, "Failed to connect")
        }
    }

    companion object {
        private const val TAG = "ConnectionController"
    }
}