package com.subreax.lightclient.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.data.DeviceRepository
import com.subreax.lightclient.data.Property
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    data class UiState(
        val deviceName: String,
        val globalProperties: List<Property>,
        val sceneProperties: List<Property>
    )

    private var _uiState by mutableStateOf(UiState("", emptyList(), emptyList()))
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
    }

    fun setPropertyValue(property: Property.ToggleProperty, value: Boolean) {
        deviceRepository.setPropertyValue(property, value)
    }

    fun setPropertyValue(property: Property.FloatRangeProperty, value: Float) {
        deviceRepository.setPropertyValue(property, value)
    }
}