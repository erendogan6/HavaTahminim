package com.erendogan6.havatahminim.core.network.di

import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.SuggestionGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Separate from [NetworkModule] because @Binds requires an abstract module. */
@Module
@InstallIn(SingletonComponent::class)
abstract class GeminiModule {
    @Binds
    abstract fun bindsSuggestionGenerator(impl: GeminiService): SuggestionGenerator
}
