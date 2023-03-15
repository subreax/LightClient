package com.subreax.lightclient.di

import android.content.Context
import com.subreax.lightclient.data.*
import com.subreax.lightclient.data.controllers.ConnectionController
import com.subreax.lightclient.data.controllers.ConnectivityController
import com.subreax.lightclient.data.controllers.SynchronizationController
import com.subreax.lightclient.data.impl.FakeConnectionRepository
import com.subreax.lightclient.data.impl.FakeConnectivityObserver
import com.subreax.lightclient.data.impl.FakeDeviceRepository
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.UiLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideConnectionRepository(
        appState: ApplicationState,
        connectivityObserver: ConnectivityObserver
    ): ConnectionRepository {
        return FakeConnectionRepository(appState, connectivityObserver)
    }

    @Provides
    @Singleton
    fun provideApplicationState(): ApplicationState {
        return ApplicationState()
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(): ConnectivityObserver {
        return FakeConnectivityObserver()
    }

    @Provides
    @Singleton
    fun provideSyncController(
        appState: ApplicationState,
        connectionRepository: ConnectionRepository,
        uiLog: UiLog
    ): SynchronizationController {
        return SynchronizationController(appState, connectionRepository, uiLog)
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
    fun provideConnectionController(
        appState: ApplicationState,
        connectionRepository: ConnectionRepository,
        uiLog: UiLog
    ): ConnectionController {
        return ConnectionController(appState, connectionRepository, uiLog)
    }

    @Singleton
    @Provides
    fun provideDeviceRepository(
        syncController: SynchronizationController
    ): DeviceRepository {
        return FakeDeviceRepository(syncController)
    }

    @Singleton
    @Provides
    fun provideUiLog(@ApplicationContext context: Context): UiLog {
        return UiLog(context)
    }
}
