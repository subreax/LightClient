package com.subreax.lightclient.data.device.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.DeviceRepository
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.state.controllers.SynchronizationController
import com.subreax.lightclient.ui.UiLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    syncController: SynchronizationController,
    private val deviceApi: DeviceApi,
    private val uiLog: UiLog
) : DeviceRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val globalPropGroup = PropertyGroup(DeviceApi.PropertyGroupId.Global, deviceApi)
    private val scenePropGroup = PropertyGroup(DeviceApi.PropertyGroupId.Scene, deviceApi)

    override val globalProperties: StateFlow<List<Property>>
        get() = globalPropGroup.props

    override val sceneProperties: StateFlow<List<Property>>
        get() = scenePropGroup.props

    private val globalPropsSyncAction = SafeSyncAction(coroutineScope) {
        showUiErrLogIfFailed(globalPropGroup.sync())
    }

    private val scenePropsSyncAction = SafeSyncAction(coroutineScope) {
        showUiErrLogIfFailed(scenePropGroup.sync())
    }


    init {
        syncController.addAction {
            coroutineScope {
                val job1 = async { globalPropGroup.sync() }
                val job2 = async { scenePropGroup.sync() }

                val result1 = job1.await()
                val result2 = job2.await()

                if (result1 is LResult.Failure) {
                    return@coroutineScope result1
                }
                result2
            }
        }

        coroutineScope.launch {
            deviceApi.propertiesChanged.collect {
                when (it) {
                    DeviceApi.PropertyGroupId.Global -> globalPropsSyncAction.run()
                    DeviceApi.PropertyGroupId.Scene -> scenePropsSyncAction.run()
                }
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


    private fun findPropertyById(id: Int): Property? {
        val sceneProp = scenePropGroup.findById(id)
        if (sceneProp != null) {
            return sceneProp
        }

        return globalPropGroup.findById(id)
    }

    private fun <T> showUiErrLogIfFailed(result: LResult<T>): LResult<T> {
        if (result is LResult.Failure) {
            uiLog.e(result.message)
        }
        return result
    }

    companion object {
        private const val TAG = "DeviceRepositoryImpl"
    }
}