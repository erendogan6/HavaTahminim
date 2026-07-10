package com.erendogan6.havatahminim.domain

import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.fixture.airQualityInfoFixture
import com.erendogan6.havatahminim.testing.fixture.pollenReadingFixture
import com.erendogan6.havatahminim.testing.repository.FakeAirQualityRepository
import com.erendogan6.havatahminim.testing.repository.FakeSuggestionRepository
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.erendogan6.havatahminim.util.PollenLevel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GenerateWeatherSuggestionUseCaseTest {
    private val airQualityRepository = FakeAirQualityRepository()
    private val suggestionRepository = FakeSuggestionRepository()
    private val resources = FakeResourcesProvider()

    private val useCase =
        GenerateWeatherSuggestionUseCase(
            airQualityRepository = airQualityRepository,
            suggestionRepository = suggestionRepository,
            resourcesProvider = resources,
            clock = TestTime.clock(),
        )

    private suspend fun invoke(
        allergens: Set<PollenType> = emptySet(),
        forceRefresh: Boolean = false,
    ) = useCase(
        lat = TestCoords.ISTANBUL_LAT,
        lon = TestCoords.ISTANBUL_LON,
        locationName = "Istanbul",
        temperature = "27°C",
        allergens = allergens,
        forceRefresh = forceRefresh,
    )

    @Test
    fun `air quality failure still generates, with an empty pollen summary`() =
        runTest {
            airQualityRepository.result = ApiResult.Error.Network
            suggestionRepository.result = ApiResult.Success("stay hydrated")

            val result = invoke(allergens = setOf(PollenType.GRASS))

            assertThat(result).isEqualTo(ApiResult.Success("stay hydrated"))
            assertThat(suggestionRepository.requests.single().pollenSummary).isEmpty()
        }

    @Test
    fun `no selected allergens means no pollen summary`() =
        runTest {
            airQualityRepository.result = ApiResult.Success(airQualityInfoFixture())

            invoke(allergens = emptySet())

            assertThat(suggestionRepository.requests.single().pollenSummary).isEmpty()
        }

    @Test
    fun `pollen-unavailable regions produce no summary either`() =
        runTest {
            airQualityRepository.result = ApiResult.Success(airQualityInfoFixture(pollenAvailable = false))

            invoke(allergens = setOf(PollenType.GRASS))

            assertThat(suggestionRepository.requests.single().pollenSummary).isEmpty()
        }

    @Test
    fun `summary lists only the selected allergens in the localized format`() =
        runTest {
            val grass = pollenReadingFixture(PollenType.GRASS, 30.0, PollenRisk.MODERATE)
            val birch = pollenReadingFixture(PollenType.BIRCH, 120.0, PollenRisk.VERY_HIGH)
            airQualityRepository.result =
                ApiResult.Success(
                    airQualityInfoFixture(
                        pollen = listOf(grass, birch),
                        hourlyByType =
                            mapOf(
                                PollenType.GRASS to (0 until 12).map { 10.0 * it },
                                PollenType.BIRCH to (0 until 12).map { 5.0 },
                            ),
                    ),
                )

            invoke(allergens = setOf(PollenType.GRASS))

            val summary = suggestionRepository.requests.single().pollenSummary
            val grassName = "res:${PollenLevel.typeNameRes(PollenType.GRASS)}"
            val moderate = "res:${PollenLevel.riskLabelRes(PollenRisk.MODERATE)}"
            val unit = "res:${R.string.pollen_unit}"
            assertThat(summary).contains("$grassName: $moderate (30 $unit)")
            assertThat(summary).doesNotContain("res:${PollenLevel.typeNameRes(PollenType.BIRCH)}")
        }

    @Test
    fun `summary slices the next six hours starting from the fixed clock`() =
        runTest {
            // hourlyTimes start exactly at TestTime — startIndex 0, next 6 values 0,10,...,50.
            airQualityRepository.result = ApiResult.Success(airQualityInfoFixture())

            invoke(allergens = setOf(PollenType.GRASS))

            assertThat(suggestionRepository.requests.single().pollenSummary)
                .contains("0, 10, 20, 30, 40, 50")
        }

    @Test
    fun `series shorter than six hours are tolerated`() =
        runTest {
            airQualityRepository.result =
                ApiResult.Success(
                    airQualityInfoFixture(
                        hourlyTimes = (0 until 3).map { TestTime.EPOCH_SECONDS + it * 3600 },
                        hourlyByType = mapOf(PollenType.GRASS to listOf(1.0, 2.0, 3.0)),
                    ),
                )

            invoke(allergens = setOf(PollenType.GRASS))

            assertThat(suggestionRepository.requests.single().pollenSummary).contains("1, 2, 3")
        }

    @Test
    fun `forceRefresh is forwarded to the suggestion repository`() =
        runTest {
            airQualityRepository.result = ApiResult.Success(airQualityInfoFixture())

            invoke(forceRefresh = true)

            assertThat(suggestionRepository.requests.single().forceRefresh).isTrue()
        }
}
