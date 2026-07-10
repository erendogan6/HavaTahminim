package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Allergy tab. Two independent slices: the Room-backed allergen selection (cold upstream shared
 * with WhileSubscribed) and the location-keyed air quality fetch. Air quality deliberately never
 * emits an interim value, so returning to the tab keeps showing the previous data while the
 * refetch runs; `null` (the splash) only appears before the very first result.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AllergyViewModel
    @Inject
    constructor(
        private val repository: WeatherRepository,
    ) : ViewModel() {
        val allergenPrefs: StateFlow<Set<PollenType>> =
            repository
                .sensitiveAllergensFlow()
                .catch { e ->
                    Log.e(TAG, "Failed to observe allergen preferences", e)
                    emit(emptySet())
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = emptySet(),
                )

        val airQuality: StateFlow<AirQualityInfo?> =
            repository.activeLocation
                .filterNotNull()
                .distinctUntilChangedBy { it.latitude to it.longitude }
                .mapLatest { location ->
                    runCall { repository.getAirQuality(location.latitude, location.longitude) }
                        .onFailure { Log.e(TAG, "Air quality fetch failed", it) }
                        .getOrNull()
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = null,
                )

        fun toggleAllergen(
            type: PollenType,
            sensitive: Boolean,
        ) {
            viewModelScope.launch {
                repository.setAllergenPreference(type, sensitive)
            }
        }

        private companion object {
            const val TAG = "AllergyViewModel"
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
