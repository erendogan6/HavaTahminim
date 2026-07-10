package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.core.data.R as DataR
import com.erendogan6.havatahminim.feature.weather.R
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel
    @Inject
    constructor(
        private val repository: WeatherRepository,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        // ---- UI state ------------------------------------------------------------------------
        // Deliberately separate streams rather than one screen-wide UiState: this ViewModel backs
        // four tabs at once, and independent slices keep an update for one tab from re-emitting
        // (and recomposing) the others. Each MutableStateFlow is exposed via asStateFlow() so no
        // caller can cast the read-only view back to a mutable one.

        private val _weatherState = MutableStateFlow<CurrentWeatherBaseResponse?>(null)
        val weatherState: StateFlow<CurrentWeatherBaseResponse?> = _weatherState.asStateFlow()

        private val _hourlyForecast = MutableStateFlow<HourlyForecastBaseResponse?>(null)
        val hourlyForecast: StateFlow<HourlyForecastBaseResponse?> = _hourlyForecast.asStateFlow()

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        private val _weatherSuggestions = MutableStateFlow<String?>(null)
        val weatherSuggestions: StateFlow<String?> = _weatherSuggestions.asStateFlow()

        private val _dailyForecast = MutableStateFlow<DailyForecastBaseResponse?>(null)
        val dailyForecast: StateFlow<DailyForecastBaseResponse?> = _dailyForecast.asStateFlow()

        private val _cities = MutableStateFlow<List<City>>(emptyList())
        val cities: StateFlow<List<City>> = _cities.asStateFlow()

        private val _location = MutableStateFlow<LocationEntity?>(null)
        val location: StateFlow<LocationEntity?> = _location.asStateFlow()

        private val _dataLoaded = MutableStateFlow(false)
        val dataLoaded: StateFlow<Boolean> = _dataLoaded.asStateFlow()

        private val _airQuality = MutableStateFlow<AirQualityInfo?>(null)
        val airQuality: StateFlow<AirQualityInfo?> = _airQuality.asStateFlow()

        /**
         * The one cold upstream this ViewModel has (a Room observation), shared as hot state.
         * WhileSubscribed(5s) stops the DB query when no screen collects it — meaningful because
         * the UI collects lifecycle-aware — and the 5s grace keeps it alive across rotation.
         * Eagerly/Lazily would pin the query for the ViewModel's whole life. The other slices are
         * results of imperative fetches, not cold streams, so MutableStateFlow (already hot, no
         * upstream to stop) is the right holder for them and stateIn does not apply.
         */
        val allergenPrefs: StateFlow<Set<PollenType>> =
            repository
                .allergenPreferences()
                .map { prefs ->
                    prefs
                        .filter { it.sensitive }
                        .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                        .toSet()
                }.catch { e ->
                    logError("Failed to observe allergen preferences", e)
                    emit(emptySet())
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = emptySet(),
                )

        // ---- In-flight work ------------------------------------------------------------------
        // A new location cancels the previous fetch fan-out so a stale response can't land after
        // (and overwrite) a fresher one. Same idea for the city search: cancelling the previous
        // query gives flatMapLatest semantics without restructuring the screen contract.

        private var refreshJob: Job? = null
        private var citiesJob: Job? = null

        // Set when allergens change; consumed the next time the ZekAI tab is opened. Confined to
        // the main thread (viewModelScope), so a plain var is race-free here.
        private var pendingAllergenRefresh = false

        init {
            loadLocation()
        }

        // ---- Public actions --------------------------------------------------------------------

        fun fetchWeatherOnce(
            lat: Double,
            lon: Double,
        ) {
            if (_dataLoaded.value || refreshJob?.isActive == true) return
            refreshJob =
                viewModelScope.launch {
                    _errorMessage.value = null
                    runCall { repository.getWeather(lat, lon) }
                        .onSuccess { response ->
                            _weatherState.value = response
                            _dataLoaded.value = true
                            fetchAdditionalData(
                                lat = lat,
                                lon = lon,
                                location = response.name,
                                temperature = resourcesProvider.getString(R.string.temperature_format, response.main.temp.toInt()),
                            )
                        }.onFailure { handleError(it, resourcesProvider.getString(DataR.string.error_fetching_weather_data)) }
                }
        }

        fun updateLocationAndFetchWeather(
            lat: Double,
            lon: Double,
        ) {
            refreshJob?.cancel()
            _dataLoaded.value = false
            saveLocation(lat, lon)
            fetchWeatherOnce(lat, lon)
        }

        fun saveLocation(
            lat: Double,
            lon: Double,
        ) {
            viewModelScope.launch {
                runCall { repository.saveLocation(lat, lon) }
                    .onSuccess { _location.value = LocationEntity(latitude = lat, longitude = lon) }
                    .onFailure { handleError(it, resourcesProvider.getString(DataR.string.error_saving_location)) }
            }
        }

        fun fetchCities(query: String) {
            citiesJob?.cancel()
            citiesJob =
                viewModelScope.launch {
                    runCall { repository.getCities(query) }
                        .onSuccess { _cities.value = it }
                        .onFailure { handleError(it, resourcesProvider.getString(DataR.string.error_fetching_cities)) }
                }
        }

        fun clearCities() {
            citiesJob?.cancel()
            _cities.value = emptyList()
        }

        fun setDataLoaded(loaded: Boolean) {
            _dataLoaded.value = loaded
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
                    temperature = resourcesProvider.getString(R.string.temperature_format, weather.main.temp.toInt()),
                    pollenSummary = buildPollenSummary(_airQuality.value, prefs),
                    lat = loc.latitude,
                    lon = loc.longitude,
                    forceRefresh = true,
                )
            }
        }

        // ---- Fetch fan-out ---------------------------------------------------------------------

        /**
         * Launched as children of [refreshJob]'s coroutine (not free-floating viewModelScope jobs),
         * so cancelling a refresh cancels the whole fan-out. Each child swallows its own failure
         * into [handleError]; siblings keep running.
         */
        private suspend fun fetchAdditionalData(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
        ) = coroutineScope {
            launch { fetchHourlyForecast(lat, lon) }
            launch { fetchDailyForecast(lat, lon) }
            launch { fetchAirQualityThenSuggestions(lat, lon, location, temperature) }
        }

        private suspend fun fetchHourlyForecast(
            lat: Double,
            lon: Double,
        ) {
            runCall { repository.getHourlyWeather(lat, lon) }
                .onSuccess { _hourlyForecast.value = it }
                .onFailure { handleError(it, resourcesProvider.getString(DataR.string.error_fetching_hourly_forecast)) }
        }

        private suspend fun fetchDailyForecast(
            lat: Double,
            lon: Double,
        ) {
            runCall { repository.getDailyWeather(lat, lon) }
                .onSuccess { _dailyForecast.value = it }
                .onFailure { handleError(it, resourcesProvider.getString(DataR.string.error_fetching_daily_forecast)) }
        }

        /**
         * Air quality is fetched before the ZekAI suggestion so the pollen/air-quality summary can
         * be folded into the Gemini prompt. A failed air-quality call must not block suggestions.
         */
        private suspend fun fetchAirQualityThenSuggestions(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
        ) {
            val airQuality = runCall { repository.getAirQuality(lat, lon) }.getOrNull()
            _airQuality.value = airQuality
            // Read the persisted selection directly to avoid a race with the prefs Flow on startup.
            val prefs = repository.sensitiveAllergens()
            fetchWeatherSuggestions(location, temperature, buildPollenSummary(airQuality, prefs), lat, lon)
        }

        private suspend fun fetchWeatherSuggestions(
            location: String,
            temperature: String,
            pollenSummary: String,
            lat: Double,
            lon: Double,
            forceRefresh: Boolean = false,
        ) {
            runCall { repository.getWeatherSuggestions(lat, lon, location, temperature, pollenSummary, forceRefresh) }
                .onSuccess { _weatherSuggestions.value = it }
                .onFailure { handleError(it, resourcesProvider.getString(DataR.string.error_fetching_weather_suggestions)) }
        }

        private fun loadLocation() {
            viewModelScope.launch {
                runCall { repository.getSavedLocation() }
                    .onSuccess { _location.value = it }
                    .onFailure { handleError(it, resourcesProvider.getString(R.string.error_fetching_location)) }
            }
        }

        // ---- Prompt building ---------------------------------------------------------------------

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

        // ---- Call plumbing ---------------------------------------------------------------------

        /**
         * Runs [call], retrying with true exponential backoff (1s, 2s, 4s) only for Gemini's
         * RESOURCE_EXHAUSTED rate-limit errors. CancellationException is rethrown, never treated
         * as a failure — a cancelled refresh must not surface an error or trigger a retry.
         */
        private suspend fun <T> runCall(call: suspend () -> T): Result<T> {
            var attempt = 0
            while (true) {
                try {
                    return Result.success(call())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val retriable =
                        attempt < MAX_RETRIES - 1 &&
                            e.message?.contains(GEMINI_RATE_LIMIT_MARKER) == true
                    if (!retriable) return Result.failure(e)
                    attempt++
                    delay(RETRY_BASE_DELAY_MS shl (attempt - 1))
                }
            }
        }

        private fun handleError(
            exception: Throwable,
            message: String,
        ) {
            _errorMessage.value = message
            logError(message, exception)
        }

        private fun logError(
            message: String,
            exception: Throwable,
        ) {
            Log.e(TAG, message, exception)
        }

        private companion object {
            const val TAG = "WeatherViewModel"
            const val STOP_TIMEOUT_MILLIS = 5_000L
            const val MAX_RETRIES = 3
            const val RETRY_BASE_DELAY_MS = 1_000L
            const val GEMINI_RATE_LIMIT_MARKER = "RESOURCE_EXHAUSTED"
        }
    }
