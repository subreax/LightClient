package com.subreax.lightclient.ui.ping

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.lightclient.data.device.repo.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PingViewModel @Inject constructor(
    deviceRepository: DeviceRepository
) : ViewModel() {
    private val device = deviceRepository.getDevice()

    var ping by mutableIntStateOf(0)
        private set

    private data class Measurement(
        val sum: Int = 0,
        val count: Int = 0
    ) {
        fun avgPing(): Int {
            return if (count > 0) {
                (sum / count.toFloat()).roundToInt()
            } else {
                0
            }
        }
    }

    private var measurement = Measurement()

    init {
        viewModelScope.launch {
            while (isActive) {
                val oldMeasurement = measurement
                measurement = Measurement(
                    sum = oldMeasurement.sum + device.ping(),
                    count = oldMeasurement.count + 1
                )
                delay(100)
            }
        }

        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                ping = measurement.avgPing()
                measurement = Measurement()
            }
        }
    }
}