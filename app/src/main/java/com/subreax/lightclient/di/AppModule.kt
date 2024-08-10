package com.subreax.lightclient.di

import android.content.Context
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.connection.impl.BleConnectionRepository
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.data.device.repo.impl.DeviceRepositoryImpl
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
        @ApplicationContext context: Context,
        deviceRepository: DeviceRepository,
        bleCentralContainer: BleCentralContainer
    ): ConnectionRepository {
        return BleConnectionRepository(context, deviceRepository, bleCentralContainer)
        //return FakeConnectionRepository()
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        @ApplicationContext context: Context,
        bleCentralContainer: BleCentralContainer
    ): DeviceRepository {
        return DeviceRepositoryImpl(bleCentralContainer, context)
    }

    @Singleton
    @Provides
    fun provideUiLog(@ApplicationContext context: Context): UiLog {
        return UiLog(context)
    }

    @Singleton
    @Provides
    fun provideBleCentralContainer(@ApplicationContext context: Context): BleCentralContainer {
        return BleCentralContainer(context)
    }
}
