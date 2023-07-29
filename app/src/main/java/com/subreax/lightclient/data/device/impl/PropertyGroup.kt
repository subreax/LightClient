package com.subreax.lightclient.data.device.impl

import android.util.Log
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.deviceapi.DeviceApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.coroutineContext

class PropertyGroup(
    val id: DeviceApi.PropertyGroupId,
    private val deviceApi: DeviceApi
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _props = MutableStateFlow<List<Property>>(emptyList())
    val props: StateFlow<List<Property>>
        get() = _props

    fun set(props: List<Property>) {
        stopSendingValueChanges()
        props.forEach {
            it.startSendingValueChanges(coroutineScope, deviceApi)
        }
        _props.value = props
    }

    fun clear() {
        stopSendingValueChanges()
        _props.value = emptyList()
    }

    fun findById(id: Int): Property? {
        return _props.value.find { it.id == id }
    }

    private fun stopSendingValueChanges() {
        _props.value.forEach {
            it.stopSendingValueChanges()
        }
    }

    suspend fun sync(): LResult<Unit> {
        Log.v(TAG, "[PropertyGroup $id] Sync started...")

        val loadingProperty = Property.SpecLoading(0f)
        set(listOf(loadingProperty))

        val propsResult = deviceApi.getProperties(id, loadingProperty.progress)
        return if (propsResult is LResult.Success) {
            coroutineContext.ensureActive()
            set(propsResult.value)
            Log.v(TAG, "[PropertyGroup $id] Sync done")
            LResult.Success(Unit)
        } else {
            val msg = (propsResult as LResult.Failure).message
            Log.e(TAG, "[PropertyGroup $id] Sync failed: $msg")
            LResult.Failure(msg)
        }
    }

    companion object {
        private const val TAG = "PropertyGroup"
    }
}