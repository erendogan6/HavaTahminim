package com.erendogan6.havatahminim.core.common.di

import android.content.Context
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @Singleton
    fun provideResourcesProvider(
        @ApplicationContext context: Context,
    ): ResourcesProvider = ResourcesProvider(context)
}
