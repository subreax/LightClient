package com.subreax.lightclient.data.state.controllers

import android.content.Context
import com.subreax.lightclient.LResult
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
    private val appContext: Context,
    private val appState: ApplicationState,
    private val connectionRepository: ConnectionRepository,
    private val uiLog: UiLog,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val actions = mutableListOf<suspend () -> LResult<Unit>>()

    fun start() {
        coroutineScope.launch {
            appState.stateId.collect {
                if (it == AppStateId.Syncing) {
                    val status = sync()
                    if (status is LResult.Success) {
                        appState.notifyEvent(AppEventId.Synced)
                    }
                    else {
                        connectionRepository.disconnect()
                        val errorMsg = (status as LResult.Failure).message
                        uiLog.e(UiText.Res(R.string.failed_to_sync, errorMsg.stringValue(appContext)))
                    }
                }
            }
        }
    }

    fun addAction(action: suspend () -> LResult<Unit>) {
        actions.add(action)
    }

    fun removeAction(action: suspend () -> LResult<Unit>) {
        actions.remove(action)
    }

    private suspend fun sync(): LResult<Unit> {
        for (action in actions) {
            val result = action()
            if (result is LResult.Failure) {
                return result
            }
        }
        return LResult.Success(Unit)
    }
}