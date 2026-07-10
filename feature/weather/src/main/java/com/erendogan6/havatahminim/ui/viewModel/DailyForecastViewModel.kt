package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.core.data.R as DataR
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.util.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

sealed interface DailyUiState {
    data object Loading : DailyUiState

    data class Error(
        val message: String,
    ) : DailyUiState

    data class Success(
        val forecast: DailyForecastBaseResponse,
    ) : DailyUiState
}

/** Daily tab. Same location-keyed pipeline as [TodayViewModel]; the repository's per-day Room cache keeps refetches cheap. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyForecastViewModel
    @Inject
    constructor(
        private val repository: WeatherRepository,
        private val resourcesProvider: ResourcesProvider,
    ) : ViewModel() {
        private var lastCoords: Pair<Double, Double>? = null

        val uiState: StateFlow<DailyUiState> =
            repository.activeLocation
                .filterNotNull()
                .distinctUntilChangedBy { it.latitude to it.longitude }
                .transformLatest { location ->
                    val coords = location.latitude to location.longitude
                    if (coords != lastCoords) emit(DailyUiState.Loading)
                    runCall { repository.getDailyWeather(location.latitude, location.longitude) }
                        .onSuccess {
                            lastCoords = coords
                            emit(DailyUiState.Success(it))
                        }.onFailure {
                            Log.e(TAG, "Daily forecast failed", it)
                            lastCoords = null
                            emit(DailyUiState.Error(resourcesProvider.getString(DataR.string.error_fetching_daily_forecast)))
                        }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = DailyUiState.Loading,
                )

        private companion object {
            const val TAG = "DailyForecastViewModel"
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
