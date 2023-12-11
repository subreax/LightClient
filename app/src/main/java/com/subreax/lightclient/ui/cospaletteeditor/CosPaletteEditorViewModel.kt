package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CosPaletteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    deviceRepository: DeviceRepository
) : ViewModel() {
    private val prop: Property.CosPalette?

    val state = CosPaletteEditorState()

    var propertyName by mutableStateOf("")
        private set

    init {
        val id = savedStateHandle.get<Int>(Screen.CosPaletteEditor.propertyIdArg)
        val device = deviceRepository.getDevice()
        prop = if (id != null) {
            device.findPropertyById(id) as Property.CosPalette
        } else {
            null
        }

        propertyName = prop?.name ?: "Палитра"

        prop?.data?.value?.let {
            state.setRed(it.red)
            state.setGreen(it.green)
            state.setBlue(it.blue)
        }
    }

    fun onPaletteChanged() {
        val red = state.getRed()
        val green = state.getGreen()
        val blue = state.getBlue()

        prop?.data?.value = CosPaletteData(red, green, blue)
    }
}