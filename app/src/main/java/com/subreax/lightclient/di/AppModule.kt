package com.subreax.lightclient.di

import android.content.Context
import com.subreax.lightclient.data.*
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.connection.impl.BleConnectionRepository
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import com.subreax.lightclient.data.connectivity.impl.BtConnectivityObserver
import com.subreax.lightclient.data.device.DeviceRepository
import com.subreax.lightclient.data.device.impl.FakeDeviceRepository
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.deviceapi.ble.BleDeviceApi
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.data.state.controllers.ConnectionController
import com.subreax.lightclient.data.state.controllers.ConnectivityController
import com.subreax.lightclient.data.state.controllers.SynchronizationController
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
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return BtConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideConnectionRepository(
        @ApplicationContext context: Context,
        appState: ApplicationState,
        deviceApi: DeviceApi
    ): ConnectionRepository {
        return BleConnectionRepository(context, appState, deviceApi)
    }

    @Provides
    @Singleton
    fun provideApplicationState(): ApplicationState {
        return ApplicationState()
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
        syncController: SynchronizationController,
        deviceApi: DeviceApi,
        uiLog: UiLog
    ): DeviceRepository {
        return FakeDeviceRepository(syncController, deviceApi, uiLog)
    }

    @Singleton
    @Provides
    fun provideUiLog(@ApplicationContext context: Context): UiLog {
        return UiLog(context)
    }

    @Singleton
    @Provides
    fun provideDeviceApi(
        @ApplicationContext context: Context,
        connectivityObserver: ConnectivityObserver
    ): DeviceApi {
        return BleDeviceApi(context, connectivityObserver)
    }
}
