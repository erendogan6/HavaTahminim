package com.erendogan6.havatahminim.core.common.di

import com.erendogan6.havatahminim.util.DefaultResourcesProvider
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds [ResourcesProvider] to its Android implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {
    @Binds
    abstract fun bindsResourcesProvider(impl: DefaultResourcesProvider): ResourcesProvider
}
