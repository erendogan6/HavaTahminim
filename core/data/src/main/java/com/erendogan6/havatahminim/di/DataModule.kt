package com.erendogan6.havatahminim.di

import com.erendogan6.havatahminim.repository.AirQualityRepository
import com.erendogan6.havatahminim.repository.AirQualityRepositoryImpl
import com.erendogan6.havatahminim.repository.AllergenRepository
import com.erendogan6.havatahminim.repository.AllergenRepositoryImpl
import com.erendogan6.havatahminim.repository.AndroidReverseGeocoder
import com.erendogan6.havatahminim.repository.LocationRepository
import com.erendogan6.havatahminim.repository.LocationRepositoryImpl
import com.erendogan6.havatahminim.repository.ReverseGeocoder
import com.erendogan6.havatahminim.repository.SuggestionRepository
import com.erendogan6.havatahminim.repository.SuggestionRepositoryImpl
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.repository.WeatherRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Repository interface bindings. Scoping lives on the @Singleton impl classes. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindsLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindsWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    abstract fun bindsAirQualityRepository(impl: AirQualityRepositoryImpl): AirQualityRepository

    @Binds
    abstract fun bindsAllergenRepository(impl: AllergenRepositoryImpl): AllergenRepository

    @Binds
    abstract fun bindsSuggestionRepository(impl: SuggestionRepositoryImpl): SuggestionRepository

    @Binds
    abstract fun bindsReverseGeocoder(impl: AndroidReverseGeocoder): ReverseGeocoder
}
