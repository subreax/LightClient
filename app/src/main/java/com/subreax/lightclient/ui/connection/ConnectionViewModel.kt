package com.subreax.lightclient.ui.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.ConnectionRepository
import com.subreax.lightclient.data.Device
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
        val devices: List<Device>
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

    val navHome = MutableSharedFlow<Device>()

    private var pickedDevice: Device? = null


    init {
        viewModelScope.launch {
            appState.stateId.collect {
                when (it) {
                    AppStateId.ConnectivityNotAvailable -> {
                        uiState = uiState.copy(waitingForConnectivity = true, devices = emptyList())
                    }
                    AppStateId.ConnectivityAvailable -> {
                        uiState = uiState.copy(waitingForConnectivity = false)
                    }
                    AppStateId.Connected -> {
                        uiState = uiState.copy(loadingMsg = UiText.Res(R.string.fetching_data))
                    }
                    AppStateId.Ready -> {
                        pickedDevice?.let { device -> navHome.emit(device) }
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            connectionRepository.devices.collect { devices ->
                uiState = uiState.copy(devices = devices)
            }
        }
    }

    fun connect(device: Device) {
        pickedDevice = device
        uiState = uiState.copy(
            loading = true,
            loadingMsg = UiText.Res(R.string.connecting_to, device.name)
        )

        viewModelScope.launch {
            val result = connectionRepository.connect(device)
            if (result is LResult.Failure) {
                uiState = uiState.copy(
                    loading = false,
                    errorMsg = ErrorMsg(System.currentTimeMillis(), result.message)
                )
            }
        }
    }
}
