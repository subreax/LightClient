package com.subreax.lightclient

import android.app.Application
import com.subreax.lightclient.data.device.DeviceRepository
import com.subreax.lightclient.data.state.controllers.ConnectionController
import com.subreax.lightclient.data.state.controllers.ConnectivityController
import com.subreax.lightclient.data.state.controllers.SynchronizationController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LightApplication : Application() {
    @Inject lateinit var connectivityController: ConnectivityController
    @Inject lateinit var connectionController: ConnectionController
    @Inject lateinit var syncController: SynchronizationController
    @Inject lateinit var deviceRepository: DeviceRepository

    override fun onCreate() {
        super.onCreate()

        connectivityController.start()
        connectionController.start()
        syncController.start()
    }
}