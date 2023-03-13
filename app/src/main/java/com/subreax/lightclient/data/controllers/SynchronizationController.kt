package com.subreax.lightclient.data.controllers

import android.util.Log
import com.subreax.lightclient.R
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SynchronizationController @Inject constructor(
    private val appState: ApplicationState,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    enum class SyncStatus {
        NotAvailable, InProcess, Done, Failed
    }

    private val coroutineScope = CoroutineScope(dispatcher)
    private val actions = mutableListOf<suspend () -> Boolean>()

    val syncStatus = MutableStateFlow(SyncStatus.NotAvailable)
    var lastError: UiText = UiText.Empty()

    fun start() {
        coroutineScope.launch {
            appState.stateId.collect {
                if (it == AppStateId.Connected) {
                    syncStatus.value = SyncStatus.InProcess
                    if (sync()) {
                        syncStatus.value = SyncStatus.Done
                        appState.notifyEvent(AppEventId.Configured)
                    }
                    else {
                        syncStatus.value = SyncStatus.Failed
                        lastError = UiText.Res(R.string.failed_to_sync)
                        Log.e("SyncController", "Failed to sync")
                    }
                }

                else if (it == AppStateId.ConnectivityAvailable) {
                    syncStatus.value = SyncStatus.NotAvailable
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