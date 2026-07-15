package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import timber.log.Timber
import javax.inject.Inject
import com.erendogan6.havatahminim.core.data.R as DataR

/**
 * Daily tab. Same location-keyed pipeline as [TodayViewModel]; the repository's per-day Room
 * cache keeps refetches cheap.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyForecastViewModel
    @Inject
    constructor(
        locationRepository: LocationRepository,
        private val weatherRepository: WeatherRepository,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        private var lastCoords: Pair<Double, Double>? = null

        val uiState: StateFlow<DailyUiState> =
            locationRepository.activeLocation
                .filterNotNull()
                .distinctUntilChangedBy { it.latitude to it.longitude }
                .transformLatest { location ->
                    val coords = location.latitude to location.longitude
                    if (coords != lastCoords) emit(DailyUiState(isLoading = true))
                    weatherRepository
                        .getDailyWeather(location.latitude, location.longitude)
                        .onSuccess {
                            lastCoords = coords
                            emit(DailyUiState(isLoading = false, forecast = it))
                        }.onError {
                            Timber.e("Daily forecast failed: $it")
                            lastCoords = null
                            val messageRes = it.userMessageRes(DataR.string.error_fetching_daily_forecast)
                            val message = resourcesProvider.getString(messageRes)
                            emit(DailyUiState(isLoading = false, error = message))
                        }
                }.stateIn(
                    scope = viewModelScope,
                    started = WhileUiSubscribed,
                    initialValue = DailyUiState(),
                )
    }
