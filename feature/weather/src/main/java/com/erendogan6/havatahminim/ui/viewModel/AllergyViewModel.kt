package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.network.onError
import com.erendogan6.havatahminim.repository.AirQualityRepository
import com.erendogan6.havatahminim.repository.AllergenRepository
import com.erendogan6.havatahminim.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

/**
 * Allergy tab: the Room-backed allergen selection plus the location-keyed air quality fetch.
 * Air quality emits no interim value, so returning to the tab keeps the previous data on screen;
 * null (the splash) only appears before the first result.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AllergyViewModel
    @Inject
    constructor(
        locationRepository: LocationRepository,
        airQualityRepository: AirQualityRepository,
        private val allergenRepository: AllergenRepository,
    ) : ViewModel() {
        val allergenPrefs: StateFlow<Set<PollenType>> =
            allergenRepository
                .sensitiveAllergensFlow()
                .catch { e ->
                    Timber.e(e, "Failed to observe allergen preferences")
                    emit(emptySet())
                }.stateIn(
                    scope = viewModelScope,
                    started = WhileUiSubscribed,
                    initialValue = emptySet(),
                )

        val airQuality: StateFlow<AirQualityInfo?> =
            locationRepository.activeLocation
                .filterNotNull()
                .distinctUntilChangedBy { it.latitude to it.longitude }
                .mapLatest { location ->
                    airQualityRepository
                        .getAirQuality(location.latitude, location.longitude)
                        .onError { Timber.e("Air quality fetch failed: $it") }
                        .getOrNull()
                }.stateIn(
                    scope = viewModelScope,
                    started = WhileUiSubscribed,
                    initialValue = null,
                )

        fun toggleAllergen(
            type: PollenType,
            sensitive: Boolean,
        ) {
            viewModelScope.launch {
                allergenRepository.setAllergenPreference(type, sensitive)
            }
        }
    }
