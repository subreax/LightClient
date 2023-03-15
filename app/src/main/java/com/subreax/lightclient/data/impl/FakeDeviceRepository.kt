package com.subreax.lightclient.data.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceRepository
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.controllers.SynchronizationController
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.ui.UiLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

class FakeDeviceRepository @Inject constructor(
    syncController: SynchronizationController,
    private val deviceApi: DeviceApi,
    private val uiLog: UiLog
) : DeviceRepository {
    private val _globalProperties = MutableStateFlow<List<Property>>(emptyList())
    override val globalProperties: Flow<List<Property>>
        get() = _globalProperties

    private val _sceneProperties = MutableStateFlow<List<Property>>(emptyList())
    override val sceneProperties: Flow<List<Property>>
        get() = _sceneProperties

    private val propertyListenerJobs = mutableMapOf<Property, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        syncController.addAction {
            propertyListenerJobs.forEach {
                it.value.cancel()
            }

            val result1 = deviceApi.getProperties(DeviceApi.PropertyGroup.Global)
            if (result1 is LResult.Success) {
                _globalProperties.value = result1.value
            } else {
                return@addAction false
            }

            val result2 = deviceApi.getProperties(DeviceApi.PropertyGroup.Scene)
            if (result2 is LResult.Success) {
                _sceneProperties.value = result2.value
            } else {
                return@addAction false
            }

            setPropertyListeners()
            true
        }

        coroutineScope.launch {
            deviceApi.propertiesChanged.collect {
                cancelScenePropertyListeners()

                val result2 = deviceApi.getProperties(DeviceApi.PropertyGroup.Scene)
                if (result2 is LResult.Success) {
                    _sceneProperties.value = result2.value
                } else {
                    uiLog.e((result2 as LResult.Failure).message)
                    // todo: probably we have to disconnect from controller
                }

                setScenePropertyListeners()
            }
        }
    }

    override suspend fun getDeviceName(): String {
        return deviceApi.getDeviceName()
    }

    override fun setPropertyValue(property: Property.ToggleProperty, value: Boolean) {
        property.toggled.value = value
    }

    override fun setPropertyValue(property: Property.FloatRangeProperty, value: Float) {
        property.current.value = value
    }

    private fun listenPropertyChanges(scope: CoroutineScope, property: Property) {
        val job = when (property) {
            is Property.FloatRangeProperty -> {
                scope.launch {
                    property.current.dropFirst().collect { value ->
                        // todo: handle error
                        deviceApi.setPropertyValue(property, value)
                    }
                }
            }

            is Property.ColorProperty -> {
                scope.launch {
                    property.color.dropFirst().collect { value ->
                        deviceApi.setPropertyValue(property, value)
                    }
                }
            }

            is Property.ToggleProperty -> {
                scope.launch {
                    property.toggled.dropFirst().collect { value ->
                        deviceApi.setPropertyValue(property, value)
                    }
                }
            }

            is Property.StringEnumProperty -> {
                scope.launch {
                    property.currentValue.dropFirst().collect { value ->
                        deviceApi.setPropertyValue(property, value)
                    }
                }
            }
        }

        propertyListenerJobs[property] = job
    }

    private fun setPropertyListeners() {
        setGlobalPropertyListeners()
        setScenePropertyListeners()
    }

    private fun setGlobalPropertyListeners() {
        _globalProperties.value.forEach { prop ->
            listenPropertyChanges(coroutineScope, prop)
        }
    }

    private fun setScenePropertyListeners() {
        _sceneProperties.value.forEach { prop ->
            listenPropertyChanges(coroutineScope, prop)
        }
    }

    private fun cancelScenePropertyListeners() {
        propertyListenerJobs.forEach {
            if (_sceneProperties.value.contains(it.key)) {
                it.value.cancel()
            }
        }
    }


    private fun <T> Flow<T>.dropFirst(): Flow<T> {
        var isFirst = true
        return transform {
            if (!isFirst) {
                emit(it)
            }
            isFirst = false
        }
    }
}