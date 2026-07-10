package com.erendogan6.havatahminim.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.safeApiCall
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location domain: the app-wide active location (in-memory SSOT — @Singleton is load-bearing),
 * its Room persistence, city search, and reverse geocoding.
 */
@Singleton
class LocationRepository
    @Inject
    constructor(
        private val locationDao: LocationDao,
        private val cityApiService: CityApiService,
        private val resourcesProvider: ResourcesProvider,
        @param:ApplicationContext private val context: Context,
    ) {
        private val language: String get() = resourcesProvider.getLanguage()

        private val _activeLocation = MutableStateFlow<LocationEntity?>(null)

        /** The location every screen keys its data off. Set by GPS, city selection, or the saved fallback. */
        val activeLocation: StateFlow<LocationEntity?> = _activeLocation.asStateFlow()

        /** Seeds [activeLocation] from the persisted location once; no-op if already set. */
        suspend fun startFromSavedLocation() {
            if (_activeLocation.value == null) {
                _activeLocation.value = runCatching { getSavedLocation() }.getOrNull()
            }
        }

        /**
         * Points the whole app at a new location. [persist] is false for the built-in fallback
         * (Istanbul) so a guessed location never overwrites the user's real saved one. Persistence
         * is best-effort: a DB write failure must not take the session location down with it.
         */
        suspend fun setActiveLocation(
            latitude: Double,
            longitude: Double,
            persist: Boolean = true,
        ) {
            _activeLocation.value = LocationEntity(latitude = latitude, longitude = longitude)
            if (persist) {
                withContext(Dispatchers.IO) {
                    runCatching { locationDao.insertLocation(LocationEntity(latitude = latitude, longitude = longitude)) }
                        .onFailure { Log.e(TAG, "Failed to persist location", it) }
                }
            }
        }

        suspend fun getSavedLocation(): LocationEntity? =
            withContext(Dispatchers.IO) {
                locationDao.getLocation()
            }

        suspend fun searchCities(query: String): ApiResult<List<City>> =
            safeApiCall {
                cityApiService.getCities(query, language = language).results ?: emptyList()
            }

        /**
         * Open-Meteo's forecast endpoint does not return a place name, so we reverse-geocode the
         * coordinates with the platform [Geocoder]. Falls back gracefully when geocoding is
         * unavailable or returns nothing.
         */
        @Suppress("DEPRECATION")
        fun resolveLocationName(
            lat: Double,
            lon: Double,
        ): String =
            try {
                val geocoder = Geocoder(context, Locale(language))
                val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                    ?: address?.countryName
                    ?: ""
            } catch (e: Exception) {
                ""
            }

        private companion object {
            const val TAG = "LocationRepository"
        }
    }
