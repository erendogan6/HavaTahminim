package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.domain.GenerateWeatherSuggestionUseCase
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.onError
import com.erendogan6.havatahminim.network.onSuccess
import com.erendogan6.havatahminim.repository.AllergenRepository
import com.erendogan6.havatahminim.repository.LocationRepository
import com.erendogan6.havatahminim.repository.WeatherRepository
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
 *    app start — no wasted Gemini calls if the tab is never opened);
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
        locationRepository: LocationRepository,
        weatherRepository: WeatherRepository,
        allergenRepository: AllergenRepository,
        private val generateSuggestion: GenerateWeatherSuggestionUseCase,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        // Main-thread confined (viewModelScope). lastPrefs detects "allergens changed since the
        // last generation" across resubscribes.
        private var lastPrefs: Set<PollenType>? = null
        private var lastCoords: Pair<Double, Double>? = null
        private var lastSuggestion: String? = null

        val suggestions: StateFlow<String?> =
            combine(
                locationRepository.activeLocation.filterNotNull(),
                allergenRepository.sensitiveAllergensFlow().catch { emit(emptySet()) },
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
                    val weather = weatherRepository.currentWeather.filterNotNull().first()
                    generateSuggestion(
                        lat = location.latitude,
                        lon = location.longitude,
                        locationName = weather.name,
                        temperature = resourcesProvider.getString(R.string.temperature_format, weather.main.temp.toInt()),
                        allergens = prefs,
                        forceRefresh = forceRefresh,
                    ).onSuccess {
                        lastCoords = coords
                        lastPrefs = prefs
                        lastSuggestion = it
                        emit(it)
                    }.onError {
                        Log.e(TAG, "Suggestion generation failed: $it")
                        emit(lastSuggestion)
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = null,
                )

        private companion object {
            const val TAG = "ZekAiViewModel"
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
