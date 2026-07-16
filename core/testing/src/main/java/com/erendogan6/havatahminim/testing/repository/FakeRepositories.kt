package com.erendogan6.havatahminim.testing.repository

import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.weather.currentforecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.dailyforecast.City
import com.erendogan6.havatahminim.model.weather.dailyforecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.hourlyforecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.repository.AirQualityRepository
import com.erendogan6.havatahminim.repository.AllergenRepository
import com.erendogan6.havatahminim.repository.LocationRepository
import com.erendogan6.havatahminim.repository.SuggestionRepository
import com.erendogan6.havatahminim.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield

/*
 * Conventions for every fake in this package:
 *  1. Suspending functions start with yield(), so Turbine can observe intermediate StateFlow
 *     emissions instead of seeing them conflated.
 *  2. Fakes honor the interface's documented contract, not just its signature.
 */

class FakeLocationRepository : LocationRepository {
    val activeLocationState = MutableStateFlow<LocationEntity?>(null)
    override val activeLocation: StateFlow<LocationEntity?> = activeLocationState.asStateFlow()

    var savedLocation: LocationEntity? = null
    val setActiveLocationCalls = mutableListOf<Triple<Double, Double, Boolean>>()
    var searchCitiesResult: ApiResult<List<City>> = ApiResult.Success(emptyList())
    val searchCitiesQueries = mutableListOf<String>()
    var locationName: String = "Istanbul"

    override suspend fun startFromSavedLocation() {
        yield()
        if (activeLocationState.value == null) {
            activeLocationState.value = savedLocation
        }
    }

    override suspend fun setActiveLocation(
        latitude: Double,
        longitude: Double,
        persist: Boolean,
    ) {
        yield()
        setActiveLocationCalls += Triple(latitude, longitude, persist)
        activeLocationState.value = LocationEntity(latitude = latitude, longitude = longitude)
    }

    override suspend fun getSavedLocation(): LocationEntity? {
        yield()
        return savedLocation
    }

    override suspend fun searchCities(query: String): ApiResult<List<City>> {
        yield()
        searchCitiesQueries += query
        return searchCitiesResult
    }

    override fun resolveLocationName(
        lat: Double,
        lon: Double,
    ): String = locationName
}

class FakeWeatherRepository : WeatherRepository {
    val currentWeatherState = MutableStateFlow<CurrentWeatherBaseResponse?>(null)
    override val currentWeather: StateFlow<CurrentWeatherBaseResponse?> = currentWeatherState.asStateFlow()

    // Handlers instead of fixed fields, so tests can gate a call on awaitCancellation().
    var refreshHandler: suspend (Double, Double) -> ApiResult<CurrentWeatherBaseResponse> =
        { _, _ -> ApiResult.Error.Unknown(null) }
    var hourlyHandler: suspend (Double, Double) -> ApiResult<HourlyForecastBaseResponse> =
        { _, _ -> ApiResult.Error.Unknown(null) }
    var dailyHandler: suspend (Double, Double) -> ApiResult<DailyForecastBaseResponse> =
        { _, _ -> ApiResult.Error.Unknown(null) }

    var refreshCallCount = 0
        private set
    var hourlyCallCount = 0
        private set
    var dailyCallCount = 0
        private set

    override suspend fun refreshCurrentWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<CurrentWeatherBaseResponse> {
        yield()
        refreshCallCount++
        val result = refreshHandler(lat, lon)
        // Contract: a successful refresh publishes into currentWeather.
        if (result is ApiResult.Success) currentWeatherState.value = result.data
        return result
    }

    override suspend fun getHourlyWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<HourlyForecastBaseResponse> {
        yield()
        hourlyCallCount++
        return hourlyHandler(lat, lon)
    }

    override suspend fun getDailyWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<DailyForecastBaseResponse> {
        yield()
        dailyCallCount++
        return dailyHandler(lat, lon)
    }
}

class FakeAirQualityRepository : AirQualityRepository {
    var result: ApiResult<AirQualityInfo> = ApiResult.Error.Unknown(null)
    var callCount = 0
        private set

    override suspend fun getAirQuality(
        lat: Double,
        lon: Double,
    ): ApiResult<AirQualityInfo> {
        yield()
        callCount++
        return result
    }
}

class FakeAllergenRepository : AllergenRepository {
    val prefs = MutableStateFlow<Set<PollenType>>(emptySet())

    /** When set, the flow throws before emitting. */
    var flowError: Throwable? = null
    val setPreferenceCalls = mutableListOf<Pair<PollenType, Boolean>>()

    override fun sensitiveAllergensFlow(): Flow<Set<PollenType>> =
        flow {
            flowError?.let { throw it }
            emitAll(prefs)
        }

    override suspend fun sensitiveAllergens(): Set<PollenType> {
        yield()
        return prefs.value
    }

    override suspend fun setAllergenPreference(
        type: PollenType,
        sensitive: Boolean,
    ) {
        yield()
        setPreferenceCalls += type to sensitive
        prefs.value = if (sensitive) prefs.value + type else prefs.value - type
    }
}

class FakeSuggestionRepository : SuggestionRepository {
    data class Request(
        val lat: Double,
        val lon: Double,
        val location: String,
        val temperature: String,
        val pollenSummary: String,
        val forceRefresh: Boolean,
    )

    var result: ApiResult<String> = ApiResult.Success("suggestion")
    val requests = mutableListOf<Request>()

    override suspend fun getSuggestions(
        lat: Double,
        lon: Double,
        location: String,
        temperature: String,
        pollenSummary: String,
        forceRefresh: Boolean,
    ): ApiResult<String> {
        yield()
        requests += Request(lat, lon, location, temperature, pollenSummary, forceRefresh)
        return result
    }
}
