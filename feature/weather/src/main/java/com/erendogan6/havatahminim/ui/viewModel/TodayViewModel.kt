package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.core.data.R as DataR
import com.erendogan6.havatahminim.network.onError
import com.erendogan6.havatahminim.network.onSuccess
import com.erendogan6.havatahminim.network.userMessageRes
import com.erendogan6.havatahminim.repository.LocationRepository
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

/**
 * Today tab. Keys off the active location: a location change restarts the pipeline
 * (transformLatest cancels the stale fetch), a resubscribe after background refreshes silently —
 * the last Success stays on screen instead of flashing the splash (Loading is only emitted when
 * the coordinates actually changed).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        locationRepository: LocationRepository,
        private val weatherRepository: WeatherRepository,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        // Confined to the main thread (viewModelScope); tracks the last rendered coordinates so
        // Loading only shows for genuine location changes.
        private var lastCoords: Pair<Double, Double>? = null

        val uiState: StateFlow<TodayUiState> =
            locationRepository.activeLocation
                .filterNotNull()
                .distinctUntilChangedBy { it.latitude to it.longitude }
                .transformLatest { location ->
                    val coords = location.latitude to location.longitude
                    if (coords != lastCoords) emit(TodayUiState.Loading)
                    weatherRepository
                        .refreshCurrentWeather(location.latitude, location.longitude)
                        .onSuccess { weather ->
                            lastCoords = coords
                            emit(TodayUiState.Success(weather, hourly = null))
                            weatherRepository
                                .getHourlyWeather(location.latitude, location.longitude)
                                .onSuccess { emit(TodayUiState.Success(weather, it)) }
                                .onError { Log.e(TAG, "Hourly forecast failed: $it") }
                        }.onError {
                            Log.e(TAG, "Weather fetch failed: $it")
                            lastCoords = null
                            emit(TodayUiState.Error(resourcesProvider.getString(it.userMessageRes(DataR.string.error_fetching_weather_data))))
                        }
                }.stateIn(
                    scope = viewModelScope,
                    started = WhileUiSubscribed,
                    initialValue = TodayUiState.Loading,
                )

        private companion object {
            const val TAG = "TodayViewModel"
        }
    }
