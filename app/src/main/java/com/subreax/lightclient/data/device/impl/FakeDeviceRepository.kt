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
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

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
    private var syncScenePropsJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        syncController.addAction {
            propertyListenerJobs.forEach {
                it.value.cancel()
            }

            val progress = MutableStateFlow(0f)
            val result1 = deviceApi.getProperties(DeviceApi.PropertyGroup.Global, progress)
            if (result1 is LResult.Success) {
                _globalProperties.value = result1.value
            } else {
                return@addAction result1 as LResult.Failure
            }

            val result2 = syncScenePropertiesAction()
            if (result2 is LResult.Failure) {
                return@addAction result2
            }

            setPropertyListeners()
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

    override fun setPropertyValue(property: Property.ToggleProperty, value: Boolean) {
        property.toggled.value = value
    }

    override fun setPropertyValue(property: Property.FloatRangeProperty, value: Float) {
        property.current.value = value
    }

    override fun setPropertyValue(property: Property.ColorProperty, value: Int) {
        property.color.value = value
    }

    override fun setPropertyValue(property: Property.StringEnumProperty, value: Int) {
        property.currentValue.value = value
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
            else -> {
                scope.launch {  }
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