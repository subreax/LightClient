package com.subreax.lightclient

import android.app.Application
import com.subreax.lightclient.data.controllers.ConnectivityController
import com.subreax.lightclient.data.controllers.SynchronizationController
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltAndroidApp
class LightApplication : Application() {
    @Inject lateinit var connectivityController: ConnectivityController
    @Inject lateinit var syncController: SynchronizationController


    override fun onCreate() {
        super.onCreate()

        connectivityController.start()
        syncController.start()
        syncController.addAction {
            delay(1000)
            true
        }
    }
}