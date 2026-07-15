package com.erendogan6.havatahminim.ui.viewModel

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject
import timber.log.Timber

/**
 * ZekAI tab. The pipeline is keyed on (location, allergens): nothing is generated until the tab
 * is first opened, an allergen change forces a regeneration on the next visit, and a plain
 * resubscribe hits the repository cache with the last suggestion kept on screen.
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
        // lastPrefs detects an allergen change across resubscribes.
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
                        // Show the thinking state; keep lastSuggestion so a failed
                        // regeneration can restore it instead of spinning forever.
                        emit(null)
                    }
                    // The prompt needs the current conditions; wait until they are published.
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
                        Timber.e("Suggestion generation failed: $it")
                        emit(lastSuggestion)
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = WhileUiSubscribed,
                    initialValue = null,
                )
    }
