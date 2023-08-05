package com.subreax.lightclient.ui.colorpickerscreen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ColorPickerViewModel @Inject constructor(
    deviceRepository: DeviceRepository,
    state: SavedStateHandle
) : ViewModel() {
    private val device = deviceRepository.getDevice()
    private val property: Property.Color

    val propertyName: String
        get() = property.name

    val propertyColor: Color
        get() = Color(property.color.value)

    init {
        val propId: Int = state[Screen.ColorPicker.propertyIdArg]!!
        val genericProp = device.findPropertyById(propId)
        if (genericProp == null) {
            Timber.e("Failed to find property with id $propId")
        }
        property = genericProp!! as Property.Color
    }

    fun setColor(color: Color) {
        property.color.value = color.toArgb()
    }
}