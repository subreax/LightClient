package com.subreax.lightclient.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.data.device.DeviceRepository
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val appState: ApplicationState
) : ViewModel() {
    data class UiState(
        val appState: AppStateId,
        val deviceName: String,
        val globalProperties: List<Property>,
        val sceneProperties: List<Property>,
        val dialogEditProperty: Property? = null
    )

    private var _uiState by mutableStateOf(UiState(AppStateId.Ready, "", emptyList(), emptyList()))
    val uiState: UiState
        get() = _uiState

    init {
        viewModelScope.launch {
            _uiState = _uiState.copy(deviceName = deviceRepository.getDeviceName())

            deviceRepository.globalProperties.collect {
                _uiState = _uiState.copy(globalProperties = it)
            }
        }

        viewModelScope.launch {
            deviceRepository.sceneProperties.collect {
                _uiState = _uiState.copy(sceneProperties = it)
            }
        }

        viewModelScope.launch {
            appState.stateId.collect {
                _uiState = _uiState.copy(appState = it)
            }
        }
    }

    fun setPropertyValue(property: Property.Bool, value: Boolean) {
        deviceRepository.setPropertyValue(property, value)
    }

    fun setPropertyValue(property: Property.FloatSlider, value: Float) {
        deviceRepository.setPropertyValue(property, value)
    }

    fun setPropertyValue(property: Property.BaseInt, value: Int) {
        if (property.type == PropertyType.Int) {
            deviceRepository.setPropertyValue(property as Property.IntNumber, value)
        }
        else {
            deviceRepository.setPropertyValue(property as Property.IntSlider, value)
        }
    }

    fun showEditDialog(property: Property) {
        _uiState = _uiState.copy(dialogEditProperty = property)
    }

    fun closeDialog() {
        _uiState = _uiState.copy(dialogEditProperty = null)
    }
}