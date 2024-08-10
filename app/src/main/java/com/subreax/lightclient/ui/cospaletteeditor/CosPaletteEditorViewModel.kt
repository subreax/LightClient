package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.CosPaletteData
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CosPaletteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    deviceRepository: DeviceRepository
) : ViewModel() {
    private val prop: Property.CosPalette

    val state = CosPaletteEditorState()

    var propertyName by mutableStateOf("")
        private set

    init {
        val id = savedStateHandle.get<Int>(Screen.CosPaletteEditor.propertyIdArg)!!
        val device = deviceRepository.getDevice()
        prop = device.findPropertyById(id) as Property.CosPalette

        propertyName = prop.name

        prop.data.value.let {
            state.red = it.red
            state.green = it.green
            state.blue = it.blue
        }
    }

    fun onPaletteChanged() {
        prop.data.value = CosPaletteData(state.red, state.green, state.blue)
    }
}