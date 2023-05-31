package com.subreax.lightclient.data.state.controllers

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConnectionController @Inject constructor(
    private val appState: ApplicationState,
    private val connectionRepository: ConnectionRepository,
    private val uiLog: UiLog,
    dispatcher: CoroutineDispatcher
) {
    private val coroutineScope = CoroutineScope(dispatcher)

    fun start() {
        coroutineScope.launch {
            appState.stateId.collect {
                when (it) {
                    AppStateId.Connecting -> {
                        tryToConnect()
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun tryToConnect() {
        val result = connectionRepository.connect()
        if (result is LResult.Success) {
            appState.notifyEvent(AppEventId.Connected)
        }
        else if (result is LResult.Failure) {
            appState.notifyEvent(AppEventId.Disconnected)
            uiLog.e(result.message)
        }
    }
}