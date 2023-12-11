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

        prop?.data12?.value?.let {
            state.setRed(it[0], it[3], it[6], it[9])
            state.setGreen(it[1], it[4], it[7], it[10])
            state.setBlue(it[2], it[5], it[8], it[11])
        }
    }

    fun onPaletteChanged() {
        val red = state.getRed()
        val green = state.getGreen()
        val blue = state.getBlue()

        /*prop?.data12?.value = arrayOf(
            red.dcOffset, red.amp, red.freq, red.phase,
            green.dcOffset, green.amp, green.freq, green.phase,
            blue.dcOffset, blue.amp, blue.freq, blue.phase
        )*/

        prop?.data12?.value = arrayOf(
            red.dcOffset, green.dcOffset, blue.dcOffset,
            red.amp, green.amp, blue.amp,
            red.freq, green.freq, blue.freq,
            red.phase, green.phase, blue.phase,
        )
    }
}