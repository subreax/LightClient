package com.subreax.lightclient.data.state

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class AppEventId {
    ConnectivityDisabled, ConnectivityEnabled, DevicePicked, Connected, Synced, Disconnected, ConnectionLost
}

enum class AppStateId {
    WaitingForConnectivity, Disconnected, Connecting, Syncing, Ready
}


class ApplicationState {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val fsm: Fsm<AppEventId>

    val stateId: Flow<AppStateId>
        get() = fsm.stateId.map { it.toAppStateId() }

    val stateIdValue: AppStateId
        get() = fsm.stateId.value.toAppStateId()

    init {
        fsm = Fsm.Builder<AppEventId>()
            .addState(AppStateId.WaitingForConnectivity) { event ->
                if (event == AppEventId.ConnectivityEnabled) {
                    setState(AppStateId.Disconnected)
                }
            }

            .addState(AppStateId.Disconnected) { event ->
                when (event) {
                    AppEventId.ConnectivityDisabled -> {
                        setState(AppStateId.WaitingForConnectivity)
                    }
                    AppEventId.DevicePicked -> {
                        setState(AppStateId.Connecting)
                    }
                    else -> {}
                }
            }

            .addState(AppStateId.Connecting) { event ->
                when (event) {
                    AppEventId.Connected -> {
                        setState(AppStateId.Syncing)
                    }
                    AppEventId.Disconnected,
                    AppEventId.ConnectivityDisabled -> {
                        setState(AppStateId.WaitingForConnectivity)
                    }
                    else -> {}
                }
            }

            .addState(AppStateId.Syncing) { event ->
                when (event) {
                    AppEventId.Synced -> {
                        setState(AppStateId.Ready)
                    }
                    AppEventId.Disconnected -> {
                        setState(AppStateId.Disconnected)
                    }
                    AppEventId.ConnectivityDisabled -> {
                        setState(AppStateId.WaitingForConnectivity)
                    }
                    else -> {}
                }
            }

            .addState(AppStateId.Ready) { event ->
                when (event) {
                    AppEventId.Disconnected -> {
                        setState(AppStateId.Disconnected)
                    }
                    AppEventId.ConnectivityDisabled,
                    AppEventId.ConnectionLost -> {
                        setState(AppStateId.Connecting)
                    }
                    else -> {}
                }
            }
            .build(AppStateId.WaitingForConnectivity.ordinal)

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

    private fun Int.toAppStateId(): AppStateId {
        return AppStateId.values()[this]
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