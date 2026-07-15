package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.network.onError
import com.erendogan6.havatahminim.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

/**
 * City search sheet. The query lives in SavedStateHandle (survives process death), the results
 * are a declarative pipeline: debounce -> flatMapLatest (a newer query cancels the in-flight
 * older one, so responses can't land out of order). Selecting a city updates the app-wide
 * active location — every screen reacts through the data layer.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CitySearchViewModel
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

        fun onQueryChange(value: String) {
            savedStateHandle[QUERY_KEY] = value
        }

        val cities: StateFlow<List<City>> =
            query
                .debounce(DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    if (q.length < MIN_QUERY_LENGTH) {
                        flowOf(emptyList())
                    } else {
                        flow {
                            val result =
                                locationRepository
                                    .searchCities(q)
                                    .onError { Timber.e("City search failed: $it") }
                            emit(result.getOrNull().orEmpty())
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = WhileUiSubscribed,
                    initialValue = emptyList(),
                )

        fun selectCity(city: City) {
            viewModelScope.launch {
                locationRepository.setActiveLocation(city.latitude, city.longitude, persist = true)
            }
        }

        private companion object {
            const val QUERY_KEY = "city_query"
            const val DEBOUNCE_MILLIS = 300L
            const val MIN_QUERY_LENGTH = 3
        }
    }
