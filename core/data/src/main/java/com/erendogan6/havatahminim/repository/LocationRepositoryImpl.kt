package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.core.common.di.IoDispatcher
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.safeApiCall
import com.erendogan6.havatahminim.util.ResourcesProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/** @Singleton is load-bearing: [activeLocation] is only meaningful if every consumer sees the same one. */
@Singleton
class LocationRepositoryImpl
    @Inject
    constructor(
        private val locationDao: LocationDao,
        private val cityApiService: CityApiService,
        private val resourcesProvider: ResourcesProvider,
        private val reverseGeocoder: ReverseGeocoder,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LocationRepository {
        private val language: String get() = resourcesProvider.getLanguage()

        private val _activeLocation = MutableStateFlow<LocationEntity?>(null)

        override val activeLocation: StateFlow<LocationEntity?> = _activeLocation.asStateFlow()

        override suspend fun startFromSavedLocation() {
            if (_activeLocation.value == null) {
                _activeLocation.value = runCatching { getSavedLocation() }.getOrNull()
            }
        }

        override suspend fun setActiveLocation(
            latitude: Double,
            longitude: Double,
            persist: Boolean,
        ) {
            _activeLocation.value = LocationEntity(latitude = latitude, longitude = longitude)
            if (persist) {
                withContext(ioDispatcher) {
                    runCatching { locationDao.insertLocation(LocationEntity(latitude = latitude, longitude = longitude)) }
                        .onFailure { Timber.e(it, "Failed to persist location") }
                }
            }
        }

        override suspend fun getSavedLocation(): LocationEntity? =
            withContext(ioDispatcher) {
                locationDao.getLocation()
            }

        override suspend fun searchCities(query: String): ApiResult<List<City>> =
            safeApiCall(ioDispatcher) {
                cityApiService.getCities(query, language = language).results ?: emptyList()
            }

        override fun resolveLocationName(
            lat: Double,
            lon: Double,
        ): String = reverseGeocoder.resolve(lat, lon, language)
    }
