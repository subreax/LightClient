package com.subreax.lightclient.di

import com.subreax.lightclient.data.*
import com.subreax.lightclient.data.controllers.ConnectivityController
import com.subreax.lightclient.data.controllers.SynchronizationController
import com.subreax.lightclient.data.impl.FakeConnectionRepository
import com.subreax.lightclient.data.impl.FakeConnectivityObserver
import com.subreax.lightclient.data.impl.FakeDeviceRepository
import com.subreax.lightclient.data.state.ApplicationState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideConnectionRepository(
        appState: ApplicationState,
        syncController: SynchronizationController
    ): ConnectionRepository {
        return FakeConnectionRepository(appState, syncController)
    }

    @Provides
    @Singleton
    fun provideApplicationState(
        connectivityObserver: ConnectivityObserver
    ): ApplicationState {
        return ApplicationState(connectivityObserver)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(): ConnectivityObserver {
        return FakeConnectivityObserver()
    }

    @Provides
    @Singleton
    fun provideSyncController(appState: ApplicationState): SynchronizationController {
        return SynchronizationController(appState)
    }

    @Singleton
    @Provides
    fun provideConnectivityController(
        appState: ApplicationState,
        connectivityObserver: ConnectivityObserver
    ): ConnectivityController {
        return ConnectivityController(appState, connectivityObserver)
    }


    @Singleton
    @Provides
    fun provideDeviceRepository(
        syncController: SynchronizationController
    ): DeviceRepository {
        return FakeDeviceRepository(syncController)
    }
}
