package com.erendogan6.havatahminim.domain

import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.network.onError
import com.erendogan6.havatahminim.repository.AirQualityRepository
import com.erendogan6.havatahminim.repository.SuggestionRepository
import com.erendogan6.havatahminim.util.PollenLevel
import com.erendogan6.havatahminim.util.ResourcesProvider
import java.time.Clock
import javax.inject.Inject
import timber.log.Timber

/**
 * Generates the ZekAI suggestion. Orchestrates two repositories — air quality feeds the prompt,
 * Gemini produces the text — plus the prompt building itself, which is why this lives in a use
 * case rather than either repository. A failed air-quality call must not block the suggestion;
 * the prompt simply omits pollen data.
 */
class GenerateWeatherSuggestionUseCase
    @Inject
    constructor(
        private val airQualityRepository: AirQualityRepository,
        private val suggestionRepository: SuggestionRepository,
        private val resourcesProvider: ResourcesProvider,
        private val clock: Clock,
    ) {
        suspend operator fun invoke(
            lat: Double,
            lon: Double,
            locationName: String,
            temperature: String,
            allergens: Set<PollenType>,
            forceRefresh: Boolean,
        ): ApiResult<String> {
            val airQuality =
                airQualityRepository
                    .getAirQuality(lat, lon)
                    .onError { Timber.e("Air quality for prompt failed: $it") }
                    .getOrNull()
            return suggestionRepository.getSuggestions(
                lat = lat,
                lon = lon,
                location = locationName,
                temperature = temperature,
                pollenSummary = buildPollenSummary(airQuality, allergens),
                forceRefresh = forceRefresh,
            )
        }

        /**
         * Allergen detail for the ZekAI prompt. Empty unless the user has selected allergens; when
         * they have, each selected allergen's current status plus the next 6 hours is included.
         */
        private fun buildPollenSummary(
            info: AirQualityInfo?,
            prefs: Set<PollenType>,
        ): String {
            if (info == null || prefs.isEmpty() || !info.pollenAvailable) return ""
            val unit = resourcesProvider.getString(R.string.pollen_unit)
            val nextLabel = resourcesProvider.getString(R.string.pollen_next_hours)
            val now = clock.millis() / 1000
            val startIndex = info.hourlyTimes.indexOfFirst { it >= now }.takeIf { it >= 0 } ?: 0

            return info.pollen
                .filter { it.type in prefs }
                .joinToString("; ") { reading ->
                    val name = resourcesProvider.getString(PollenLevel.typeNameRes(reading.type))
                    val currentRisk = resourcesProvider.getString(PollenLevel.riskLabelRes(reading.risk))
                    val currentValue = (reading.valueGrains ?: 0.0).toInt()
                    val series = info.hourlyByType[reading.type].orEmpty()
                    val next6 =
                        (startIndex until startIndex + 6)
                            .mapNotNull { series.getOrNull(it) }
                            .joinToString(", ") { it.toInt().toString() }
                    "$name: $currentRisk ($currentValue $unit); $nextLabel: $next6 $unit"
                }
        }
    }
