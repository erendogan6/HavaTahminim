package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-level concerns only: seeding/steering the active location and exposing the current weather
 * for activity chrome (the full-bleed background and the nav bar's visibility gate). Everything
 * screen-specific lives in the per-screen ViewModels.
 */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val repository: WeatherRepository,
    ) : ViewModel() {
        val currentWeather: StateFlow<CurrentWeatherBaseResponse?> = repository.currentWeather

        /** GPS fix or a picked city: point the whole app at it and persist it. */
        fun setLocation(
            lat: Double,
            lon: Double,
        ) {
            viewModelScope.launch {
                repository.setActiveLocation(lat, lon, persist = true)
            }
        }

        /**
         * No GPS available: fall back to the persisted location, or Istanbul as a last resort.
         * The fallback is not persisted so it never overwrites a real saved location.
         */
        fun startFromSavedOrDefault() {
            viewModelScope.launch {
                repository.startFromSavedLocation()
                if (repository.activeLocation.value == null) {
                    repository.setActiveLocation(DEFAULT_LAT, DEFAULT_LON, persist = false)
                }
            }
        }

        private companion object {
            // Default location: Istanbul coordinates
            const val DEFAULT_LAT = 41.0082
            const val DEFAULT_LON = 28.9784
        }
    }
