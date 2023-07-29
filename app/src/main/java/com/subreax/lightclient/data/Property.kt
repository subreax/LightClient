package com.subreax.lightclient.data

import android.util.Log
import com.subreax.lightclient.data.deviceapi.DeviceApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

enum class PropertyType {
    Color, IntNumber, IntSlider, IntSmallHSlider, FloatNumber, FloatSlider, FloatSmallHSlider, Enum, Bool, Special
}

sealed class Property(val id: Int, val type: PropertyType, val name: String) {
    private var valueChangeListenerJob: Job? = null

    abstract fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi): Job

    fun startSendingValueChanges(scope: CoroutineScope, deviceApi: DeviceApi) {
        if (valueChangeListenerJob == null) {
            valueChangeListenerJob = createValueChangeListener(scope, deviceApi)
            Log.v("Property", "Start sending value changes of property '$name'")
        }
    }

    fun stopSendingValueChanges() {
        valueChangeListenerJob?.cancel()
        valueChangeListenerJob = null
        Log.v("Property", "Stop sending value changes of property '$name'")
    }

    class SpecLoading(
        initialProgress: Float
    ) : Property(-1, PropertyType.Special, "Loading in progress") {
        val progress = MutableStateFlow(initialProgress)

        override fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi) = Job()
    }


    class Enum(
        id: Int,
        name: String,
        val values: List<String>,
        initialValue: Int,
    ) : Property(id, PropertyType.Enum, name) {
        val currentValue = MutableStateFlow(initialValue)

        override fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi): Job {
            return scope.launch {
                currentValue.drop(1).collect {
                    deviceApi.updatePropertyValue(this@Enum)
                }
            }
        }
    }

    open class BaseFloat(
        id: Int,
        type: PropertyType,
        name: String,
        initialValue: Float,
        val min: Float,
        val max: Float
    ) : Property(id, type, name) {
        val current = MutableStateFlow(initialValue)

        override fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi): Job {
            return scope.launch {
                current.drop(1).collect {
                    deviceApi.updatePropertyValue(this@BaseFloat)
                }
            }
        }
    }

    class FloatSlider(
        id: Int,
        name: String,
        initialValue: Float,
        min: Float,
        max: Float
    ) : BaseFloat(id, PropertyType.FloatSlider, name, initialValue, min, max)

    class FloatNumber(
        id: Int,
        name: String,
        initialValue: Float,
        min: Float,
        max: Float
    ) : BaseFloat(id, PropertyType.FloatNumber, name, initialValue, min, max)

    class FloatSmallHSlider(
        id: Int,
        name: String,
        initialValue: Float,
        min: Float,
        max: Float
    ) : BaseFloat(id, PropertyType.FloatSmallHSlider, name, initialValue, min, max)

    class Color(
        id: Int,
        name: String,
        initialValue: Int
    ) : Property(id, PropertyType.Color, name) {
        val color = MutableStateFlow(initialValue)

        override fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi): Job {
            return scope.launch {
                color.drop(1).collect {
                    deviceApi.updatePropertyValue(this@Color)
                }
            }
        }
    }

    class Bool(
        id: Int,
        name: String,
        initialValue: Boolean
    ) : Property(id, PropertyType.Bool, name) {
        val toggled = MutableStateFlow(initialValue)

        override fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi): Job {
            return scope.launch {
                toggled.drop(1).collect {
                    deviceApi.updatePropertyValue(this@Bool)
                }
            }
        }
    }

    open class BaseInt(
        id: Int,
        type: PropertyType,
        name: String,
        initialValue: Int,
        val min: Int,
        val max: Int
    ) : Property(id, type, name) {
        val current = MutableStateFlow(initialValue)

        override fun createValueChangeListener(scope: CoroutineScope, deviceApi: DeviceApi): Job {
            return scope.launch {
                current.drop(1).collect {
                    deviceApi.updatePropertyValue(this@BaseInt)
                }
            }
        }
    }

    class IntNumber(
        id: Int,
        name: String,
        initialValue: Int,
        min: Int,
        max: Int
    ) : BaseInt(id, PropertyType.IntNumber, name, initialValue, min, max)

    class IntSlider(
        id: Int,
        name: String,
        initialValue: Int,
        min: Int,
        max: Int
    ) : BaseInt(id, PropertyType.IntSlider, name, initialValue, min, max)

    class IntSmallHSlider(
        id: Int,
        name: String,
        initialValue: Int,
        min: Int,
        max: Int
    ) : BaseInt(id, PropertyType.IntSmallHSlider, name, initialValue, min, max)
}
