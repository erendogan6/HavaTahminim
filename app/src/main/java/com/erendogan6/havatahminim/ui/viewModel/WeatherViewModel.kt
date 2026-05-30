package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.util.PollenLevel
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel
    @Inject
    constructor(
        private val repository: WeatherRepository,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        private val _weatherState = MutableStateFlow<CurrentWeatherBaseResponse?>(null)
        val weatherState: StateFlow<CurrentWeatherBaseResponse?> = _weatherState

        private val _hourlyForecast = MutableStateFlow<HourlyForecastBaseResponse?>(null)
        val hourlyForecast: StateFlow<HourlyForecastBaseResponse?> = _hourlyForecast

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

        private val _weatherSuggestions = MutableStateFlow<String?>(null)
        val weatherSuggestions: StateFlow<String?> = _weatherSuggestions

        private val _dailyForecast = MutableStateFlow<DailyForecastBaseResponse?>(null)
        val dailyForecast: StateFlow<DailyForecastBaseResponse?> = _dailyForecast

        private val _cities = MutableStateFlow<List<City>>(emptyList())
        val cities: StateFlow<List<City>> get() = _cities

        private val _location = MutableStateFlow<LocationEntity?>(null)
        val location: StateFlow<LocationEntity?> get() = _location

        private val _dataLoaded = MutableStateFlow(false)
        val dataLoaded: StateFlow<Boolean> = _dataLoaded

        private val _airQuality = MutableStateFlow<AirQualityInfo?>(null)
        val airQuality: StateFlow<AirQualityInfo?> = _airQuality

        private val _allergenPrefs = MutableStateFlow<Set<PollenType>>(emptySet())
        val allergenPrefs: StateFlow<Set<PollenType>> = _allergenPrefs

        // Set when allergens change; consumed (and reset) the next time the ZekAI tab is opened.
        private var pendingAllergenRefresh = false

        init {
            loadLocation()
            observeAllergenPreferences()
        }

        private fun observeAllergenPreferences() {
            viewModelScope.launch {
                repository.allergenPreferences().collect { prefs ->
                    _allergenPrefs.value =
                        prefs
                            .filter { it.sensitive }
                            .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                            .toSet()
                }
            }
        }

        fun toggleAllergen(
            type: PollenType,
            sensitive: Boolean,
        ) {
            viewModelScope.launch {
                repository.setAllergenPreference(type, sensitive)
            }
            // Don't hit ZekAI now; just mark it stale. It refreshes when the ZekAI tab is opened.
            pendingAllergenRefresh = true
        }

        /**
         * Called when the ZekAI tab is opened. Regenerates the suggestion (bypassing the cache) only
         * if the user changed their allergens since the last one — so the request is sent on tab
         * open, not while toggling chips.
         */
        fun onZekAIOpened() {
            if (!pendingAllergenRefresh) return
            val weather = _weatherState.value ?: return
            val loc = _location.value ?: return
            pendingAllergenRefresh = false
            _weatherSuggestions.value = null
            viewModelScope.launch {
                val prefs = repository.sensitiveAllergens()
                fetchWeatherSuggestions(
                    location = weather.name,
                    temperature = "${weather.main.temp.toInt()}°C",
                    pollenSummary = buildPollenSummary(_airQuality.value, prefs),
                    lat = loc.latitude,
                    lon = loc.longitude,
                    forceRefresh = true,
                )
            }
        }

        fun setDataLoaded(loaded: Boolean) {
            _dataLoaded.value = loaded
        }

        private fun loadLocation() {
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.getSavedLocation() },
                    onSuccess = { location ->
                        _location.value = location
                    },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_fetching_location)) },
                )
            }
        }

        fun saveLocation(
            lat: Double,
            lon: Double,
        ) {
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.saveLocation(lat, lon) },
                    onSuccess = { _location.value = LocationEntity(latitude = lat, longitude = lon) },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_saving_location)) },
                )
            }
        }

        fun updateLocationAndFetchWeather(
            lat: Double,
            lon: Double,
        ) {
            _dataLoaded.value = false
            saveLocation(lat, lon)
            fetchWeatherOnce(lat, lon)
        }

        fun fetchWeatherOnce(
            lat: Double,
            lon: Double,
        ) {
            if (_dataLoaded.value) return
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.getWeather(lat, lon) },
                    onSuccess = { response ->
                        _weatherState.value = response
                        _errorMessage.value = null
                        fetchAdditionalData(lat, lon, response.name, "${response.main.temp.toInt()}°C")
                        logDebug("Weather data fetched successfully", response)
                        _dataLoaded.value = true
                    },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_fetching_weather_data)) },
                )
            }
        }

        fun clearCities() {
            _cities.value = emptyList()
        }

        private fun fetchAdditionalData(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
        ) {
            fetchHourlyForecast(lat, lon)
            fetchDailyForecast(lat, lon)
            fetchAirQualityThenSuggestions(lat, lon, location, temperature)
        }

        /**
         * Air quality is fetched before the ZekAI suggestion so the pollen/air-quality summary can
         * be folded into the Gemini prompt. A failed air-quality call must not block suggestions.
         */
        private fun fetchAirQualityThenSuggestions(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
        ) {
            viewModelScope.launch {
                val airQuality = runCatching { repository.getAirQuality(lat, lon) }.getOrNull()
                _airQuality.value = airQuality
                logDebug("Air quality fetched", airQuality)
                // Read the persisted selection directly to avoid a race with the prefs Flow on startup.
                val prefs = repository.sensitiveAllergens()
                fetchWeatherSuggestions(location, temperature, buildPollenSummary(airQuality, prefs), lat, lon)
            }
        }

        /**
         * Allergen detail for the ZekAI prompt. Empty unless the user has selected allergens; when
         * they have, each selected allergen's current status plus the next 6 hours is included.
         */
        private fun buildPollenSummary(
            info: AirQualityInfo?,
            prefs: Set<PollenType>,
        ): String {
            if (info == null || prefs.isEmpty() || !info.pollenAvailable) return ""
            val unit = resourcesProvider.getString(R.string.pollen_unit)
            val nextLabel = resourcesProvider.getString(R.string.pollen_next_hours)
            val now = System.currentTimeMillis() / 1000
            val startIndex = info.hourlyTimes.indexOfFirst { it >= now }.takeIf { it >= 0 } ?: 0

            return info.pollen
                .filter { it.type in prefs }
                .joinToString("; ") { reading ->
                    val name = resourcesProvider.getString(PollenLevel.typeNameRes(reading.type))
                    val currentRisk = resourcesProvider.getString(PollenLevel.riskLabelRes(reading.risk))
                    val currentValue = (reading.valueGrains ?: 0.0).toInt()
                    val series = info.hourlyByType[reading.type].orEmpty()
                    val next6 =
                        (startIndex until startIndex + 6)
                            .mapNotNull { series.getOrNull(it) }
                            .joinToString(", ") { it.toInt().toString() }
                    "$name: $currentRisk ($currentValue $unit); $nextLabel: $next6 $unit"
                }
        }

        private fun fetchHourlyForecast(
            lat: Double,
            lon: Double,
        ) {
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.getHourlyWeather(lat, lon) },
                    onSuccess = { response ->
                        _hourlyForecast.value = response
                        _errorMessage.value = null
                        logDebug("Hourly forecast data fetched successfully", response)
                    },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_fetching_hourly_forecast)) },
                )
            }
        }

        private fun fetchWeatherSuggestions(
            location: String,
            temperature: String,
            pollenSummary: String,
            lat: Double,
            lon: Double,
            forceRefresh: Boolean = false,
        ) {
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.getWeatherSuggestions(lat, lon, location, temperature, pollenSummary, forceRefresh) },
                    onSuccess = { suggestions ->
                        _weatherSuggestions.value = suggestions
                        _errorMessage.value = null
                        logDebug("Weather suggestions fetched successfully", suggestions)
                    },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_fetching_weather_suggestions)) },
                )
            }
        }

        private fun fetchDailyForecast(
            lat: Double,
            lon: Double,
        ) {
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.getDailyWeather(lat, lon) },
                    onSuccess = { response ->
                        _dailyForecast.value = response
                        _errorMessage.value = null
                        logDebug("Daily forecast data fetched successfully", response)
                    },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_fetching_daily_forecast)) },
                )
            }
        }

        fun fetchCities(query: String) {
            viewModelScope.launch {
                handleApiCall(
                    call = { repository.getCities(query) },
                    onSuccess = { cities ->
                        _cities.value = cities
                        logDebug("Cities fetched successfully", cities)
                    },
                    onError = { handleError(it, resourcesProvider.getString(R.string.error_fetching_cities)) },
                )
            }
        }

        private suspend fun <T> handleApiCall(
            call: suspend () -> T,
            onSuccess: (T) -> Unit,
            onError: (Exception) -> Unit,
        ) {
            val maxRetries = 3
            var currentRetry = 0

            while (currentRetry < maxRetries) {
                try {
                    val response = call()
                    onSuccess(response)
                    return
                } catch (e: Exception) {
                    if (e.message?.contains("RESOURCE_EXHAUSTED") == true) {
                        currentRetry++
                        println("Retrying due to RESOURCE_EXHAUSTED... Attempt: $currentRetry")
                        delay(1000L * currentRetry) // Exponential backoff
                    } else {
                        println("Error during API call: ${e.message}")
                        onError(e)
                        return
                    }
                }
            }
            onError(Exception("Max retries exceeded"))
        }

        private fun handleError(
            exception: Exception,
            logMessage: String,
        ) {
            _errorMessage.value = logMessage
            logError(logMessage, exception)
        }

        private fun logDebug(
            message: String,
            data: Any?,
        ) {
            Log.d("WeatherViewModel", "$message: $data")
        }

        private fun logError(
            message: String,
            exception: Exception,
        ) {
            Log.e("WeatherViewModel", message, exception)
        }
    }
