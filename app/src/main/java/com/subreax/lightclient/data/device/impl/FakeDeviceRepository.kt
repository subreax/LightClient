package com.subreax.lightclient.data.device.impl

import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.DeviceRepository
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.state.controllers.SynchronizationController
import com.subreax.lightclient.ui.UiLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class FakeDeviceRepository @Inject constructor(
    syncController: SynchronizationController,
    private val deviceApi: DeviceApi,
    private val uiLog: UiLog
) : DeviceRepository {
    private val _globalProperties = MutableStateFlow<List<Property>>(emptyList())
    override val globalProperties: StateFlow<List<Property>>
        get() = _globalProperties

    private val _sceneProperties = MutableStateFlow<List<Property>>(emptyList())
    override val sceneProperties: StateFlow<List<Property>>
        get() = _sceneProperties

    private val propertyListenerJobs = mutableMapOf<Property, Job>()
    private var syncScenePropsJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        Log.d(TAG, "FakeDeviceRepository init")
        syncController.addAction {
            propertyListenerJobs.forEach {
                it.value.cancel()
            }

            val globalPropsLoadingProp = Property.SpecLoading(0f)
            _globalProperties.value = listOf(
                globalPropsLoadingProp
            )
            _sceneProperties.value = listOf(
                Property.SpecLoading(0f)
            )

            val result1 = deviceApi.getProperties(
                DeviceApi.PropertyGroup.Global,
                globalPropsLoadingProp.progress
            )
            if (result1 is LResult.Success) {
                _globalProperties.value = result1.value
            } else {
                return@addAction result1 as LResult.Failure
            }

            val result2 = syncScenePropertiesAction()
            if (result2 is LResult.Failure) {
                return@addAction result2
            }

            setGlobalPropertyListeners()

            LResult.Success(Unit)
        }

        coroutineScope.launch {
            deviceApi.propertiesChanged.collect {
                syncSceneProperties()
            }
        }
    }

    override suspend fun getDeviceName(): String {
        return deviceApi.getDeviceName()
    }

    override fun getPropertyById(id: Int): LResult<Property> {
        val prop = findPropertyById(id)
        return if (prop != null)
            LResult.Success(prop)
        else
            LResult.Failure("Property #$id not found")
    }

    override fun setPropertyValue(property: Property.Bool, value: Boolean) {
        property.toggled.value = value
    }

    override fun setPropertyValue(property: Property.FloatSlider, value: Float) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.IntNumber, value: Int) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.IntSlider, value: Int) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.Color, value: Int) {
        property.color.value = value
    }

    override fun setPropertyValue(property: Property.Enum, value: Int) {
        property.currentValue.value = value
    }

    private fun listenPropertyChanges(scope: CoroutineScope, property: Property) {
        Log.v(TAG, "Listen to changes of ${property.name}")
        val job = when (property) {
            is Property.FloatSlider -> {
                scope.launch {
                    property.current.dropFirst().collect {
                        deviceApi.updatePropertyValue(property)
                    }
                }
            }

            is Property.Color -> {
                scope.launch {
                    property.color.dropFirst().collect {
                        deviceApi.updatePropertyValue(property)
                    }
                }
            }

            is Property.Bool -> {
                scope.launch {
                    property.toggled.dropFirst().collect {
                        deviceApi.updatePropertyValue(property)
                    }
                }
            }

            is Property.Enum -> {
                scope.launch {
                    property.currentValue.dropFirst().collect {
                        deviceApi.updatePropertyValue(property)
                    }
                }
            }

            is Property.IntNumber -> {
                scope.launch {
                    property.current.dropFirst().collect {
                        deviceApi.updatePropertyValue(property)
                    }
                }
            }

            is Property.IntSlider -> {
                scope.launch {
                    property.current.dropFirst().collect {
                        deviceApi.updatePropertyValue(property)
                    }
                }
            }

            else -> {
                scope.launch {  }
            }
        }

        propertyListenerJobs[property] = job
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

    private fun findPropertyById(id: Int): Property? {
        val sceneProp = _sceneProperties.value.find { it.id == id }
        if (sceneProp != null) {
            return sceneProp
        }

        return _globalProperties.value.find { it.id == id }
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

    private suspend fun syncSceneProperties() {
        syncScenePropsJob?.let {
            Log.v(TAG, "Cancelling sync job")
            it.cancel()
            it.join()
            Log.v(TAG, "Cancel done")
        }

        Log.v(TAG, "Starting sync job")
        syncScenePropsJob = coroutineScope.launch {
            try {
                syncScenePropertiesAction()
            } finally {
                if (isActive) {
                    syncScenePropsJob = null
                }
            }
        }
    }

    private suspend fun syncScenePropertiesAction(): LResult<Unit> {
        Log.v(TAG, "Sync scene properties...")

        cancelScenePropertyListeners()

        val loadingProperty = Property.SpecLoading(0f)
        _sceneProperties.value = listOf(loadingProperty)

        val props: List<Property>
        val result2 = deviceApi.getProperties(DeviceApi.PropertyGroup.Scene, loadingProperty.progress)
        if (result2 is LResult.Success) {
            props = result2.value
        } else {
            val msg = (result2 as LResult.Failure).message
            uiLog.e(msg)
            return LResult.Failure(msg)
        }

        coroutineContext.ensureActive()
        _sceneProperties.value = props
        setScenePropertyListeners()

        Log.v(TAG, "Sync done")
        return LResult.Success(Unit)
    }

    companion object {
        private const val TAG = "FakeDeviceRepository"
    }
}