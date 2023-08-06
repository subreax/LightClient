package com.subreax.lightclient.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.data.device.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    deviceRepository: DeviceRepository
) : ViewModel() {
    data class UiState(
        val deviceState: Device.State,
        val deviceName: String,
        val globalProperties: List<Property>,
        val sceneProperties: List<Property>,
        val dialogEditProperty: Property? = null
    )

    private lateinit var device: Device

    var uiState by mutableStateOf(
        UiState(
            Device.State.Ready,
            "",
            emptyList(),
            emptyList()
        )
    )
        private set

    private val _navBack = MutableStateFlow(false)
    val navBack: Flow<Boolean> = _navBack

    init {
        if (deviceRepository.isConnected()) {
            device = deviceRepository.getDevice()

            viewModelScope.launch {
                uiState = uiState.copy(deviceName = device.getDeviceDesc().name)

                device.globalProperties.collect {
                    uiState = uiState.copy(globalProperties = it)
                }
            }

            viewModelScope.launch {
                device.sceneProperties.collect {
                    uiState = uiState.copy(sceneProperties = it)
                }
            }

            viewModelScope.launch {
                device.state.collect {
                    uiState = uiState.copy(deviceState = it)
                }
            }
        }
        else {
            _navBack.value = true
        }
    }

    fun setPropertyValue(property: Property.Bool, value: Boolean) {
        property.toggled.value = value
    }

    fun setPropertyValue(property: Property.BaseFloat, value: Float) {
        property.current.value = value
    }

    fun setPropertyValue(property: Property.BaseInt, value: Int) {
        property.current.value = value
    }

    fun showEditDialog(property: Property) {
        uiState = uiState.copy(dialogEditProperty = property)
    }

    fun closeDialog() {
        uiState = uiState.copy(dialogEditProperty = null)
    }

    fun navBackHandled() {
        _navBack.value = false
    }
}