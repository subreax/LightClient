package com.subreax.lightclient.ui.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.R
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.except.UiTextException
import com.subreax.lightclient.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiConnectionState {
    object Idle : UiConnectionState()
    data class Connecting(val deviceName: String) : UiConnectionState()
    data class Fetching(val deviceName: String) : UiConnectionState()
}

sealed class UiConnectionEvents {
    data class NavHome(val deviceDesc: DeviceDesc) : UiConnectionEvents()
    data class ConnectError(val message: UiText) : UiConnectionEvents()
}

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {
    val devices = connectionRepository.devices

    var connectionState by mutableStateOf<UiConnectionState>(UiConnectionState.Idle)

    private val _events = MutableSharedFlow<UiConnectionEvents>()
    val events = _events.asSharedFlow()

    fun connect(deviceDesc: DeviceDesc) = viewModelScope.launch {
        connectionRepository.connect(deviceDesc)
            .catch {
                connectionState = UiConnectionState.Idle
                if (it is UiTextException) {
                    _events.emit(UiConnectionEvents.ConnectError(it.details))
                } else {
                    _events.emit(UiConnectionEvents.ConnectError(UiText.Hardcoded(it.toString())))
                }
            }
            .collect {
                when (it) {
                    Device.State.Connecting -> {
                        connectionState = UiConnectionState.Connecting(deviceDesc.name)
                    }

                    Device.State.Fetching -> {
                        connectionState = UiConnectionState.Fetching(deviceDesc.name)
                    }

                    Device.State.Ready -> {
                        connectionState = UiConnectionState.Idle

                        _events.emit(UiConnectionEvents.NavHome(deviceDesc))
                    }

                    else -> {
                        connectionState = UiConnectionState.Idle

                        val msg = UiText.Res(R.string.failed_to_connect, deviceDesc.name)
                        _events.emit(UiConnectionEvents.ConnectError(msg))
                    }
                }
            }
    }
}
