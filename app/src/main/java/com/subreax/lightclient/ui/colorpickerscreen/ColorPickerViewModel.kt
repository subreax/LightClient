package com.subreax.lightclient.ui.colorpickerscreen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.ui.colorpicker.ColorPickerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ColorPickerViewModel @Inject constructor(
    deviceRepository: DeviceRepository,
    state: SavedStateHandle
) : ViewModel() {
    private val device = deviceRepository.getDevice()
    private val property: Property.Color

    init {
        val propId: Int = state[Screen.ColorPicker.propertyIdArg]!!
        val genericProp = device.findPropertyById(propId)
        if (genericProp == null) {
            Timber.e("Failed to find property with id $propId")
        }
        property = genericProp!! as Property.Color
    }

    val propertyName: String
        get() = property.name

    val propertyColor: Color
        get() = Color(property.color.value)

    val colorPickerState = ColorPickerState(
        initialColor = propertyColor,
        onUpdate = {
            setColor(it)
        }
    )

    private val _colorLibrary = MutableStateFlow(emptyList<Color>())
    val colorLibrary = _colorLibrary.asStateFlow()

    fun setColor(color: Color) {
        property.color.value = color.toArgb()
    }

    fun addColorToLibrary(color: Color) {
        _colorLibrary.value += color
    }
}