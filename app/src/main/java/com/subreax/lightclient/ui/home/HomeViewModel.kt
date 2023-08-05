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

    private val device = deviceRepository.getDevice()

    var uiState by mutableStateOf(
        UiState(
            Device.State.Ready,
            "",
            device.globalProperties.value,
            device.sceneProperties.value
        )
    )
        private set

    init {
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
}