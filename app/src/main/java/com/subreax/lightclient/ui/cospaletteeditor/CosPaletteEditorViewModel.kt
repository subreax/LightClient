package com.subreax.lightclient.ui.cospaletteeditor

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
    private val prop = getProperty(deviceRepository, savedStateHandle)
    val propertyName = prop.name

    val cosPaletteEditorState = cosPaletteEditorStateFrom(
        prop = prop,
        onCosineDragged = this::onCosineDragged
    )

    val cosFieldsEditorState = CosineFieldsEditorState(
        initCosine = Cosine(),
        onCosineChanged = this::onCosineFieldsChanged
    )

    fun selectCosine(id: CosPaletteEditorState.CosineId) {
        cosPaletteEditorState.select(id)
        cosFieldsEditorState.cosine = cosPaletteEditorState.selectedCosine
    }

    private fun onCosineDragged() {
        cosFieldsEditorState.cosine = cosPaletteEditorState.selectedCosine
    }

    private fun onCosineFieldsChanged(cosine: Cosine) {
        cosPaletteEditorState.selectedCosine = cosine
    }

    companion object {
        private fun getProperty(
            deviceRepository: DeviceRepository,
            savedStateHandle: SavedStateHandle
        ): Property.CosPalette {
            val id = savedStateHandle.get<Int>(Screen.CosPaletteEditor.propertyIdArg)!!
            val device = deviceRepository.getDevice()
            return device.findPropertyById(id) as Property.CosPalette
        }

        private fun cosPaletteEditorStateFrom(
            prop: Property.CosPalette,
            onCosineDragged: () -> Unit
        ): CosPaletteEditorState {
            return with(prop.data.value) {
                CosPaletteEditorState(red, green, blue, onCosineDragged)
            }
        }
    }
}