package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.network.ApiResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Location domain: the app-wide active location (in-memory SSOT), its Room persistence, city
 * search, and reverse geocoding.
 */
interface LocationRepository {
    /** The location every screen keys its data off. Set by GPS, city selection, or the saved fallback. */
    val activeLocation: StateFlow<LocationEntity?>

    /** Seeds [activeLocation] from the persisted location once; no-op if already set. */
    suspend fun startFromSavedLocation()

    /**
     * Points the whole app at a new location. [persist] is false for the built-in fallback
     * (Istanbul) so a guessed location never overwrites the user's real saved one. Persistence
     * is best-effort: a DB write failure must not take the session location down with it.
     */
    suspend fun setActiveLocation(
        latitude: Double,
        longitude: Double,
        persist: Boolean = true,
    )

    suspend fun getSavedLocation(): LocationEntity?

    suspend fun searchCities(query: String): ApiResult<List<City>>

    /** Best human-readable name for the coordinates in the current language, or "". */
    fun resolveLocationName(
        lat: Double,
        lon: Double,
    ): String
}
