package com.erendogan6.havatahminim.di

import com.erendogan6.havatahminim.util.AndroidConnectivityChecker
import com.erendogan6.havatahminim.util.ConnectivityChecker
import com.erendogan6.havatahminim.util.DeviceLocationSource
import com.erendogan6.havatahminim.util.FusedDeviceLocationSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the app module's platform seams to their Android implementations. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppModule {
    @Binds
    abstract fun bindDeviceLocationSource(impl: FusedDeviceLocationSource): DeviceLocationSource

    @Binds
    abstract fun bindConnectivityChecker(impl: AndroidConnectivityChecker): ConnectivityChecker
}
