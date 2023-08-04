package com.subreax.lightclient.ui.enumscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.subreax.lightclient.Screen
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.repo.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EnumPickerViewModel @Inject constructor(
    deviceRepository: DeviceRepository,
    state: SavedStateHandle
) : ViewModel() {
    private val device = deviceRepository.getDevice()
    private val property: Property.Enum

    val propertyName: String
        get() = property.name

    val enumerators: List<String>
        get() = property.values

    val selected: Flow<Int>
        get() = property.currentValue

    init {
        val propId: Int = state[Screen.EnumPicker.propertyIdArg]!!
        val genericProp = device.findPropertyById(propId)
        if (genericProp == null) {
            Log.e("EnumPickerVM", "Property with id $propId not found")
        }
        property = genericProp!! as Property.Enum
    }

    fun select(index: Int) {
        property.currentValue.value = index
    }
}