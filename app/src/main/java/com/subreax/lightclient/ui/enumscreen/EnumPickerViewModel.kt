package com.subreax.lightclient.ui.enumscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.LResult
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EnumPickerViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    state: SavedStateHandle
) : ViewModel() {
    private val property: Property.Enum

    val propertyName: String
        get() = property.name

    val enumerators: List<String>
        get() = property.values

    val selected: Flow<Int>
        get() = property.currentValue

    init {
        val propId: Int = state[Screen.EnumPicker.propertyIdArg]!!
        val rawProp = (deviceRepository.getPropertyById(propId) as LResult.Success).value
        property = rawProp as Property.Enum
    }

    fun select(index: Int) {
        deviceRepository.setPropertyValue(property, index)
    }
}