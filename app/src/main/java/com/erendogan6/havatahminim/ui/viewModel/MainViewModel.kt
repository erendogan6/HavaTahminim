package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
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
 * App-level orchestration: seeds/steers the active location at start-up (GPS → saved → Istanbul
 * fallback), drives the offline dialog, and exposes the current weather for activity chrome (the
 * full-bleed background and the nav bar's visibility gate). Screen-specific state lives in the
 * per-screen ViewModels.
 *
 * The platform touchpoints — positioning and connectivity — sit behind the [DeviceLocationSource]
 * and [ConnectivityChecker] seams, so this whole start-up decision tree is plain JVM-testable code
 * rather than logic stranded in the Activity. The Compose layer only owns the permission launchers
 * (which are bound to the activity result registry) and reports their outcome here.
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

        /**
         * Location permission is available (already held at launch, or just granted): resolve a GPS
         * fix and point the whole app at it, falling back to the saved/default location when no fix
         * can be obtained.
         */
        fun onLocationPermissionGranted() {
            viewModelScope.launch { resolveLocation() }
        }

        /** Location permission denied: start from the saved location, or Istanbul. */
        fun onLocationPermissionDenied() {
            viewModelScope.launch { startFromSavedOrDefault() }
        }

        /** The Today screen's "use my location" action, once permission is confirmed. */
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
         * Fall back to the persisted location, or Istanbul as a last resort. The fallback is not
         * persisted so it never overwrites a real saved location. Surfaces the offline dialog when
         * there is no connection, since the resulting screen will be cache-only.
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
            // Default location: Istanbul coordinates
            const val DEFAULT_LAT = 41.0082
            const val DEFAULT_LON = 28.9784
        }
    }
