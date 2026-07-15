package com.erendogan6.havatahminim.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * Time as an injected [Clock] instead of `System.currentTimeMillis()`, so tests can pin it with
 * `Clock.fixed(...)`.
 */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun providesClock(): Clock = Clock.systemDefaultZone()
}
