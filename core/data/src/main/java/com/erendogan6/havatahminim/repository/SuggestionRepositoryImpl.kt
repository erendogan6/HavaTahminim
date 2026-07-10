package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.core.common.di.IoDispatcher
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.SuggestionGenerator
import com.erendogan6.havatahminim.network.safeApiCall
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.erendogan6.havatahminim.util.distanceMeters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionRepositoryImpl
    @Inject
    constructor(
        private val suggestionGenerator: SuggestionGenerator,
        private val weatherSuggestionDao: WeatherSuggestionDao,
        private val resourcesProvider: ResourcesProvider,
        private val clock: Clock,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SuggestionRepository {
        override suspend fun getSuggestions(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
            pollenSummary: String,
            forceRefresh: Boolean,
        ): ApiResult<String> =
            safeApiCall(ioDispatcher) {
                val cachedSuggestion = weatherSuggestionDao.getLatestSuggestion()
                val currentLanguage = resourcesProvider.getLanguage()

                if (cachedSuggestion != null && !forceRefresh) {
                    val cacheIsFresh =
                        distanceMeters(cachedSuggestion.latitude, cachedSuggestion.longitude, lat, lon) <=
                            SUGGESTION_DISTANCE_THRESHOLD_METERS &&
                            clock.millis() - cachedSuggestion.timestamp <= SUGGESTION_TIME_THRESHOLD_MILLIS &&
                            cachedSuggestion.language == currentLanguage
                    if (cacheIsFresh) return@safeApiCall cachedSuggestion.suggestion
                }

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
                    withRateLimitRetry { suggestionGenerator.generate(userMessage) }
                        ?: error("Gemini returned an empty response")

                weatherSuggestionDao.insertSuggestion(
                    WeatherSuggestionEntity(
                        location = location,
                        temperature = temperature,
                        suggestion = suggestion,
                        latitude = lat,
                        longitude = lon,
                        timestamp = clock.millis(),
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
            const val SUGGESTION_DISTANCE_THRESHOLD_METERS = 5000.0 // 5 km
            const val SUGGESTION_TIME_THRESHOLD_MILLIS = 2 * 60 * 60 * 1000L // 2 hours
            const val MAX_RETRIES = 3
            const val RETRY_BASE_DELAY_MS = 1_000L
            const val GEMINI_RATE_LIMIT_MARKER = "RESOURCE_EXHAUSTED"
        }
    }
