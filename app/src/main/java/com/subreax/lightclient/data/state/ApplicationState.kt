package com.subreax.lightclient.data.state

import android.util.Log
import com.subreax.lightclient.data.ConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class AppEventId {
    ConnectivityDisabled, ConnectivityEnabled, Connected, Configured, Disconnected, ConnectionLost
}

enum class AppStateId {
    ConnectivityNotAvailable, ConnectivityAvailable, Connected, Ready, Reconnecting
}


class ApplicationState(
    private val connectivityObserver: ConnectivityObserver
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val fsm: Fsm<AppEventId>

    val stateId: Flow<AppStateId>
        get() = fsm.stateId.map { AppStateId.values()[it] }

    init {
        fsm = Fsm.Builder<AppEventId>()
            .addState(AppStateId.ConnectivityNotAvailable) { event ->
                if (event == AppEventId.ConnectivityEnabled) {
                    setState(AppStateId.ConnectivityAvailable)
                }
            }

            .addState(AppStateId.ConnectivityAvailable) { event ->
                when (event) {
                    AppEventId.Connected -> {
                        setState(AppStateId.Connected)
                    }
                    AppEventId.ConnectivityDisabled -> {
                        setState(AppStateId.ConnectivityNotAvailable)
                    }
                    else -> {}
                }
            }

            .addState(AppStateId.Connected) { event ->
                when (event) {
                    AppEventId.Configured -> {
                        setState(AppStateId.Ready)
                    }
                    AppEventId.Disconnected -> {
                        setState(AppStateId.ConnectivityAvailable)
                    }
                    AppEventId.ConnectivityDisabled -> {
                        setState(AppStateId.ConnectivityNotAvailable)
                    }
                    else -> {}
                }
            }

            .addState(AppStateId.Ready) { event ->
                when (event) {
                    AppEventId.Disconnected -> {
                        setState(AppStateId.ConnectivityAvailable)
                    }
                    AppEventId.ConnectivityDisabled -> {
                        setState(AppStateId.Reconnecting)
                    }
                    AppEventId.ConnectionLost -> {
                        setState(AppStateId.Reconnecting)
                    }
                    else -> {}
                }
            }

            .addState(AppStateId.Reconnecting) { event ->
                when (event) {
                    AppEventId.Connected -> {
                        setState(AppStateId.Connected)
                    }
                    AppEventId.Disconnected -> {
                        if (connectivityObserver.isAvailable) {
                            setState(AppStateId.ConnectivityAvailable)
                        } else {
                            setState(AppStateId.ConnectivityNotAvailable)
                        }
                    }
                    else -> {}
                }
            }
            .build(AppStateId.ConnectivityNotAvailable.ordinal)

        coroutineScope.launch {
            stateId.collect {
                Log.v("AppState", "state changed: $it")
            }
        }
    }


    fun notifyEvent(event: AppEventId) {
        Log.v("AppState", "notifying event: $event")
        fsm.notifyEvent(event)
    }
}


fun <E> Fsm.Builder<E>.addState(
    id: AppStateId,
    onEvent: FsmEventHandler<E>
): Fsm.Builder<E> {
    return addState(id.ordinal, onEvent)
}

fun <E> Fsm<E>.setState(id: AppStateId) {
    setState(id.ordinal)
}