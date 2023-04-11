package com.subreax.lightclient.ui.colorpickerscreen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.LResult
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ColorPickerViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    state: SavedStateHandle
) : ViewModel() {
    private val property: Property.Color

    val propertyName: String
    val propertyColor: Color

    init {
        val propId: Int = state[Screen.ColorPicker.propertyIdArg]!!
        val propertyResult = deviceRepository.getPropertyById(propId)
        if (propertyResult is LResult.Success) {
            property = propertyResult.value as Property.Color
            propertyName = property.name
        }
        else {
            throw Exception((propertyResult as LResult.Failure).message.toString())
        }

        val intColor = property.color.value
        propertyColor = Color(intColor)
    }

    fun setColor(color: Color) {
        deviceRepository.setPropertyValue(property, color.toArgb())
    }
}