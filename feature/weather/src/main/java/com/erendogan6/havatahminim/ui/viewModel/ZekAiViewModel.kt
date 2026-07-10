package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.util.PollenLevel
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

/**
 * ZekAI tab. The suggestion is a function of (location, sensitive allergens): the pipeline is
 * keyed on exactly that pair, so
 *  - opening the tab the first time generates the suggestion (nothing is generated eagerly at
 *    app start anymore — no wasted Gemini calls if the tab is never opened);
 *  - changing allergens on the Allergy tab does nothing immediately; the regeneration happens
 *    when this tab is next subscribed, with forceRefresh bypassing the repository cache;
 *  - a plain resubscribe (tab switch / background) with unchanged inputs re-runs the pipeline but
 *    hits the repository's 2h/5km suggestion cache, and the last suggestion stays on screen
 *    (no interim null) so there is no spinner flash.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ZekAiViewModel
    @Inject
    constructor(
        private val repository: WeatherRepository,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        // Main-thread confined (viewModelScope). lastPrefs detects "allergens changed since the
        // last generation" across resubscribes — the successor of the old pendingAllergenRefresh.
        private var lastPrefs: Set<PollenType>? = null
        private var lastCoords: Pair<Double, Double>? = null
        private var lastSuggestion: String? = null

        val suggestions: StateFlow<String?> =
            combine(
                repository.activeLocation.filterNotNull(),
                repository.sensitiveAllergensFlow().catch { emit(emptySet()) },
            ) { location, prefs -> location to prefs }
                .distinctUntilChanged { old, new ->
                    old.first.latitude == new.first.latitude &&
                        old.first.longitude == new.first.longitude &&
                        old.second == new.second
                }.transformLatest { (location, prefs) ->
                    val coords = location.latitude to location.longitude
                    val forceRefresh = lastPrefs != null && lastPrefs != prefs
                    if (coords != lastCoords || forceRefresh) {
                        // Genuine input change: show the thinking state while regenerating.
                        emit(null)
                        lastSuggestion = null
                    }
                    // The prompt needs the current conditions; wait until the Today pipeline (or a
                    // previous session) has published them.
                    val weather = repository.currentWeather.filterNotNull().first()
                    val airQuality =
                        runCall { repository.getAirQuality(location.latitude, location.longitude) }
                            .onFailure { Log.e(TAG, "Air quality for prompt failed", it) }
                            .getOrNull()
                    runCall {
                        repository.getWeatherSuggestions(
                            lat = location.latitude,
                            lon = location.longitude,
                            location = weather.name,
                            temperature = resourcesProvider.getString(R.string.temperature_format, weather.main.temp.toInt()),
                            pollenSummary = buildPollenSummary(airQuality, prefs),
                            forceRefresh = forceRefresh,
                        )
                    }.onSuccess {
                        lastCoords = coords
                        lastPrefs = prefs
                        lastSuggestion = it
                        emit(it)
                    }.onFailure {
                        Log.e(TAG, "Suggestion generation failed", it)
                        emit(lastSuggestion)
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = null,
                )

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

        private companion object {
            const val TAG = "ZekAiViewModel"
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
