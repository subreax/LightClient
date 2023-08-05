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
import com.subreax.lightclient.ui.UiLog
import com.subreax.lightclient.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ErrorMsg(
    val time: Long,
    val msg: UiText
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val uiLog: UiLog
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

    private val _navHome = MutableSharedFlow<DeviceDesc>()
    val navHome: Flow<DeviceDesc>
        get() = _navHome


    init {
        viewModelScope.launch {
            connectionRepository.devices.collect { devices ->
                uiState = uiState.copy(devices = devices)
            }
        }
    }

    fun connect(deviceDesc: DeviceDesc) {
        viewModelScope.launch {
            _connect(deviceDesc)
        }
    }

    private suspend fun _connect(deviceDesc: DeviceDesc) {
        connectionRepository.connect(deviceDesc)
            .catch {
                uiState = uiState.copy(loading = false)
                if (it is UiTextException) {
                    uiLog.e(it.details)
                } else {
                    uiLog.e(UiText.Hardcoded(it.toString()))
                }
            }
            .collect {
                when (it) {
                    Device.State.Connecting -> {
                        uiState = uiState.copy(
                            loading = true,
                            loadingMsg = UiText.Res(R.string.connecting_to, deviceDesc.name)
                        )
                    }

                    Device.State.Fetching -> {
                        uiState = uiState.copy(
                            loading = true,
                            loadingMsg = UiText.Res(R.string.fetching_data)
                        )
                    }

                    Device.State.Ready -> {
                        _navHome.emit(deviceDesc)
                    }

                    else -> {
                        uiState = uiState.copy(loading = false)
                        uiLog.e(UiText.Res(R.string.failed_to_connect, deviceDesc.name))
                    }
                }
            }
    }
}
