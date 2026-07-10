package com.erendogan6.havatahminim.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * Wall-clock time as an injected dependency. Anything time-dependent (cache TTLs, "today" keys,
 * "upcoming hour" cutoffs) reads this [Clock] instead of `System.currentTimeMillis()`, so tests
 * pin time with `Clock.fixed(...)`.
 */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun providesClock(): Clock = Clock.systemDefaultZone()
}
