package com.subreax.lightclient.data.state.controllers

import android.util.Log
import com.subreax.lightclient.R
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiLog
import com.subreax.lightclient.ui.UiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SynchronizationController @Inject constructor(
    private val appState: ApplicationState,
    private val connectionRepository: ConnectionRepository,
    private val uiLog: UiLog,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val actions = mutableListOf<suspend () -> Boolean>()

    fun start() {
        coroutineScope.launch {
            appState.stateId.collect {
                if (it == AppStateId.Syncing) {
                    if (sync()) {
                        appState.notifyEvent(AppEventId.Synced)
                    }
                    else {
                        connectionRepository.disconnect()
                        uiLog.e(UiText.Res(R.string.failed_to_sync))
                        Log.e("SyncController", "Failed to sync")
                    }
                }
            }
        }
    }

    fun addAction(action: suspend () -> Boolean) {
        actions.add(action)
    }

    fun removeAction(action: suspend () -> Boolean) {
        actions.remove(action)
    }

    private suspend fun sync(): Boolean {
        for (action in actions) {
            if (!action()) {
                return false
            }
        }
        return true
    }
}