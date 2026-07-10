package com.erendogan6.havatahminim.repository

import android.location.Location
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.safeApiCall
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import com.erendogan6.havatahminim.util.ResourcesProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ZekAI suggestion domain: the Gemini call plus its Room cache. The cache is invalidated when the
 * location moves beyond 5km, the entry is older than 2h, the device language changes, or the
 * caller forces a refresh (allergen selection changed).
 */
@Singleton
class SuggestionRepository
    @Inject
    constructor(
        private val geminiService: GeminiService,
        private val weatherSuggestionDao: WeatherSuggestionDao,
        private val resourcesProvider: ResourcesProvider,
    ) {
        suspend fun getSuggestions(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
            pollenSummary: String = "",
            forceRefresh: Boolean = false,
        ): ApiResult<String> =
            safeApiCall {
                val cachedSuggestion = weatherSuggestionDao.getLatestSuggestion()
                val currentLanguage = resourcesProvider.getLanguage()

                val needsNewSuggestion =
                    forceRefresh ||
                        cachedSuggestion?.let {
                            val savedLocation =
                                Location("saved").apply {
                                    latitude = it.latitude
                                    longitude = it.longitude
                                }
                            val currentLocation =
                                Location("current").apply {
                                    latitude = lat
                                    longitude = lon
                                }
                            val distance = savedLocation.distanceTo(currentLocation)
                            val timeElapsed = System.currentTimeMillis() - it.timestamp

                            distance > SUGGESTION_DISTANCE_THRESHOLD_METERS ||
                                timeElapsed > SUGGESTION_TIME_THRESHOLD_MILLIS ||
                                it.language != currentLanguage
                        } ?: true

                if (!needsNewSuggestion) return@safeApiCall cachedSuggestion!!.suggestion

                weatherSuggestionDao.deleteAllSuggestions()

                // The persona/instructions live in the model's systemInstruction (see GeminiService);
                // here we only send the user-specific data.
                val userMessage =
                    buildString {
                        append("Konum: $location\nSıcaklık: $temperature")
                        if (pollenSummary.isNotBlank()) {
                            append("\nSeçili alerjenlerin polen durumu: $pollenSummary")
                        }
                    }
                val suggestion =
                    withRateLimitRetry { geminiService.model.generateContent(userMessage).text }
                        ?: error("Gemini returned an empty response")

                weatherSuggestionDao.insertSuggestion(
                    WeatherSuggestionEntity(
                        location = location,
                        temperature = temperature,
                        suggestion = suggestion,
                        latitude = lat,
                        longitude = lon,
                        language = currentLanguage,
                    ),
                )
                suggestion
            }

        /**
         * Gemini's free tier rate-limits with RESOURCE_EXHAUSTED; retry with exponential backoff
         * (1s/2s/4s). Cancellation is rethrown — never retried, never swallowed.
         */
        private suspend fun <T> withRateLimitRetry(block: suspend () -> T): T {
            var attempt = 0
            while (true) {
                try {
                    return block()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val retriable =
                        attempt < MAX_RETRIES - 1 &&
                            e.message?.contains(GEMINI_RATE_LIMIT_MARKER) == true
                    if (!retriable) throw e
                    attempt++
                    delay(RETRY_BASE_DELAY_MS shl (attempt - 1))
                }
            }
        }

        private companion object {
            const val SUGGESTION_DISTANCE_THRESHOLD_METERS = 5000 // 5 km
            const val SUGGESTION_TIME_THRESHOLD_MILLIS = 2 * 60 * 60 * 1000 // 2 hours
            const val MAX_RETRIES = 3
            const val RETRY_BASE_DELAY_MS = 1_000L
            const val GEMINI_RATE_LIMIT_MARKER = "RESOURCE_EXHAUSTED"
        }
    }
