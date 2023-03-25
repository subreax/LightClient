package com.subreax.lightclient.ui.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.R
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ErrorMsg(
    val time: Long,
    val msg: UiText
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val appState: ApplicationState
) : ViewModel() {
    data class UiState(
        val loading: Boolean,
        val loadingMsg: UiText,
        val errorMsg: ErrorMsg,
        val waitingForConnectivity: Boolean,
        val devices: List<DeviceDesc>
    )

    var uiState by mutableStateOf(
        UiState(
            false,
            UiText.Empty(),
            ErrorMsg(System.currentTimeMillis(), UiText.Empty()),
            false,
            emptyList()
        )
    )
        private set

    val navHome = MutableSharedFlow<DeviceDesc>()

    private var selectedDevice: DeviceDesc? = null


    init {
        viewModelScope.launch {
            appState.stateId.collect {
                when (it) {
                    AppStateId.WaitingForConnectivity -> {
                        uiState = uiState.copy(waitingForConnectivity = true, devices = emptyList())
                    }
                    AppStateId.Disconnected -> {
                        uiState = uiState.copy(waitingForConnectivity = false, loading = false)
                    }
                    AppStateId.Connecting -> {
                        uiState = uiState.copy(
                            loading = true,
                            loadingMsg = UiText.Res(R.string.connecting_to, selectedDevice?.name ?: "unknown")
                        )
                    }
                    AppStateId.Syncing -> {
                        uiState = uiState.copy(loadingMsg = UiText.Res(R.string.fetching_data))
                    }
                    AppStateId.Ready -> {
                        //pickedDevice?.let { device -> navHome.emit(device) }
                    }
                }
            }
        }

        viewModelScope.launch {
            connectionRepository.devices.collect { devices ->
                uiState = uiState.copy(devices = devices)
            }
        }
    }

    fun connect(deviceDesc: DeviceDesc) {
        selectedDevice = deviceDesc


        viewModelScope.launch {
            connectionRepository.selectDevice(deviceDesc)
        }
    }
}
