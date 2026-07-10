package com.erendogan6.havatahminim.testing.service

import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityResponse
import com.erendogan6.havatahminim.model.weather.openmeteo.GeoSearchResponse
import com.erendogan6.havatahminim.model.weather.openmeteo.OpenMeteoResponse
import com.erendogan6.havatahminim.network.AirQualityApiService
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.SuggestionGenerator
import com.erendogan6.havatahminim.network.WeatherApiService
import kotlinx.coroutines.yield

/*
 * Fake rules: every suspending function starts with `yield()` (dispatch point for ordered
 * StateFlow assertions); `nextError` is thrown once by whichever call comes next, letting tests
 * exercise the ApiResult error taxonomy end-to-end through the real repositories.
 */

class FakeWeatherApiService : WeatherApiService {
    var currentResponse = OpenMeteoResponse()
    var hourlyResponse = OpenMeteoResponse()
    var dailyResponse = OpenMeteoResponse()
    var nextError: Exception? = null

    var currentCallCount = 0
        private set
    var hourlyCallCount = 0
        private set
    var dailyCallCount = 0
        private set

    private fun throwPending() {
        nextError?.let {
            nextError = null
            throw it
        }
    }

    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        current: String,
        daily: String,
        timezone: String,
        timeformat: String,
        days: Int,
    ): OpenMeteoResponse {
        yield()
        currentCallCount++
        throwPending()
        return currentResponse
    }

    override suspend fun getHourlyWeather(
        lat: Double,
        lon: Double,
        hourly: String,
        daily: String,
        timezone: String,
        timeformat: String,
        days: Int,
    ): OpenMeteoResponse {
        yield()
        hourlyCallCount++
        throwPending()
        return hourlyResponse
    }

    override suspend fun getDailyWeather(
        lat: Double,
        lon: Double,
        daily: String,
        timezone: String,
        timeformat: String,
        days: Int,
    ): OpenMeteoResponse {
        yield()
        dailyCallCount++
        throwPending()
        return dailyResponse
    }
}

class FakeCityApiService : CityApiService {
    var response = GeoSearchResponse(results = null)
    var nextError: Exception? = null
    val requests = mutableListOf<Pair<String, String>>()

    override suspend fun getCities(
        query: String,
        language: String,
        count: Int,
        format: String,
    ): GeoSearchResponse {
        yield()
        requests += query to language
        nextError?.let {
            nextError = null
            throw it
        }
        return response
    }
}

class FakeAirQualityApiService : AirQualityApiService {
    var response = AirQualityResponse()
    var nextError: Exception? = null
    var callCount = 0
        private set

    override suspend fun getAirQuality(
        lat: Double,
        lon: Double,
        current: String,
        hourly: String,
        timezone: String,
        timeformat: String,
        days: Int,
    ): AirQualityResponse {
        yield()
        callCount++
        nextError?.let {
            nextError = null
            throw it
        }
        return response
    }
}

/**
 * Scripted Gemini stand-in: each call consumes the next [Result] from [script] (the last entry
 * repeats once drained, so single-entry scripts behave like a fixed response).
 */
class FakeSuggestionGenerator : SuggestionGenerator {
    val requests = mutableListOf<String>()
    val script = ArrayDeque<Result<String?>>()

    override suspend fun generate(userMessage: String): String? {
        yield()
        requests += userMessage
        val step = if (script.size > 1) script.removeFirst() else script.first()
        return step.getOrThrow()
    }

    companion object {
        fun rateLimited(): Result<String?> =
            Result.failure(RuntimeException("429 RESOURCE_EXHAUSTED: quota exceeded"))

        fun success(text: String?): Result<String?> = Result.success(text)
    }
}
