package com.subreax.lightclient.data

import com.subreax.lightclient.data.device.api.DeviceApi
import com.subreax.lightclient.ui.cospaletteeditor.CosPaletteData
import com.subreax.lightclient.ui.cospaletteeditor.Cosine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import timber.log.Timber

enum class PropertyType {
    Color, IntNumber, IntSlider, IntSmallHSlider, FloatNumber, FloatSlider, FloatSmallHSlider, Enum, Bool, CosPalette, Special
}

sealed class Property(val id: Int, val type: PropertyType, val name: String) {
    private var valueChangeListenerJob: Job? = null

    abstract fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job

    fun startSendingValueChanges(scope: CoroutineScope, api: DeviceApi) {
        if (valueChangeListenerJob == null) {
            valueChangeListenerJob = createValueChangeListener(scope, api)
            Timber.v("Start sending value changes of property '$name'")
        }
    }

    fun stopSendingValueChanges() {
        valueChangeListenerJob?.cancel()
        valueChangeListenerJob = null
        Timber.v("Stop sending value changes of property '$name'")
    }

    class SpecLoading(
        initialProgress: Float
    ) : Property(-1, PropertyType.Special, "Loading in progress") {
        val progress = MutableStateFlow(initialProgress)

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi) = Job()
    }


    class Enum(
        id: Int,
        name: String,
        val values: List<String>,
        initialValue: Int,
    ) : Property(id, PropertyType.Enum, name) {
        val currentValue = MutableStateFlow(initialValue)

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job {
            return scope.launch {
                currentValue.drop(1).collect {
                    api.uploadPropertyValue(this@Enum)
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

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job {
            return scope.launch {
                current.drop(1).collect {
                    api.uploadPropertyValue(this@BaseFloat)
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

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job {
            return scope.launch {
                color.drop(1).collect {
                    api.uploadPropertyValue(this@Color)
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

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job {
            return scope.launch {
                toggled.drop(1).collect {
                    api.uploadPropertyValue(this@Bool)
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

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job {
            return scope.launch {
                current.drop(1).collect {
                    api.uploadPropertyValue(this@BaseInt)
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

    class CosPalette(
        id: Int,
        name: String,
        initialData: CosPaletteData
    ) : Property(id, PropertyType.CosPalette, name) {
        val data = MutableStateFlow(initialData)

        override fun createValueChangeListener(scope: CoroutineScope, api: DeviceApi): Job {
            return scope.launch {
                data.drop(1).collect {
                    api.uploadPropertyValue(this@CosPalette)
                }
            }
        }

        companion object {
            val NO_DATA = CosPaletteData(
                red = Cosine(0f, 1f, 1f, 0f),
                green = Cosine(0f, 1f, 1f, 0f),
                blue = Cosine(0f, 1f, 1f, 0f)
            )
        }
    }
}
