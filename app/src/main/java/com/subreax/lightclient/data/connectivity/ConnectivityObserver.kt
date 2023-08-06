package com.subreax.lightclient.data.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    enum class Event {
        NotAvailable, Available, Connected, Disconnected
    }

    val status: Flow<Event>
}