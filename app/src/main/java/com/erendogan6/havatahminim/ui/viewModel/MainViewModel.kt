package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.weather.currentforecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.repository.LocationRepository
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.util.ConnectivityChecker
import com.erendogan6.havatahminim.util.DeviceLocationSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the start-up location decision (GPS, saved, or the Istanbul fallback), the offline dialog
 * and the current weather used by the activity chrome. Positioning and connectivity sit behind
 * [DeviceLocationSource] and [ConnectivityChecker]; the UI layer only reports permission results.
 */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
        private val deviceLocationSource: DeviceLocationSource,
        private val connectivityChecker: ConnectivityChecker,
        weatherRepository: WeatherRepository,
    ) : ViewModel() {
        val currentWeather: StateFlow<CurrentWeatherBaseResponse?> = weatherRepository.currentWeather

        private val _showNoInternetDialog = MutableStateFlow(false)
        val showNoInternetDialog: StateFlow<Boolean> = _showNoInternetDialog.asStateFlow()

        fun dismissNoInternetDialog() {
            _showNoInternetDialog.value = false
        }

        /** Resolves a GPS fix and makes it the active location; falls back to saved/default. */
        fun onLocationPermissionGranted() {
            viewModelScope.launch { resolveLocation() }
        }

        /** Starts from the saved location, or Istanbul. */
        fun onLocationPermissionDenied() {
            viewModelScope.launch { startFromSavedOrDefault() }
        }

        /** "Use my location" action; assumes permission is already granted. */
        fun useCurrentLocation() {
            viewModelScope.launch {
                if (!connectivityChecker.isOnline()) {
                    _showNoInternetDialog.value = true
                    return@launch
                }
                deviceLocationSource.currentLocation()?.let {
                    locationRepository.setActiveLocation(it.latitude, it.longitude, persist = true)
                }
            }
        }

        private suspend fun resolveLocation() {
            val fix = if (connectivityChecker.isOnline()) deviceLocationSource.currentLocation() else null
            if (fix != null) {
                locationRepository.setActiveLocation(fix.latitude, fix.longitude, persist = true)
            } else {
                startFromSavedOrDefault()
            }
        }

        /**
         * Saved location, or Istanbul as a last resort. The fallback is not persisted so it can't
         * overwrite a real saved location.
         */
        private suspend fun startFromSavedOrDefault() {
            if (!connectivityChecker.isOnline()) {
                _showNoInternetDialog.value = true
            }
            locationRepository.startFromSavedLocation()
            if (locationRepository.activeLocation.value == null) {
                locationRepository.setActiveLocation(DEFAULT_LAT, DEFAULT_LON, persist = false)
            }
        }

        private companion object {
            const val DEFAULT_LAT = 41.0082
            const val DEFAULT_LON = 28.9784
        }
    }
