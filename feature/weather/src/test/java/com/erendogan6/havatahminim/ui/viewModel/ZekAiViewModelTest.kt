package com.erendogan6.havatahminim.ui.viewModel

import app.cash.turbine.test
import com.erendogan6.havatahminim.domain.GenerateWeatherSuggestionUseCase
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.fixture.airQualityInfoFixture
import com.erendogan6.havatahminim.testing.fixture.currentWeatherFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeAirQualityRepository
import com.erendogan6.havatahminim.testing.repository.FakeAllergenRepository
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeSuggestionRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ZekAiViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val allergenRepository = FakeAllergenRepository()
    private val airQualityRepository = FakeAirQualityRepository()
    private val suggestionRepository = FakeSuggestionRepository()
    private val resources = FakeResourcesProvider()

    private fun viewModel(): ZekAiViewModel {
        val useCase =
            GenerateWeatherSuggestionUseCase(
                airQualityRepository = airQualityRepository,
                suggestionRepository = suggestionRepository,
                resourcesProvider = resources,
                clock = TestTime.clock(),
            )
        return ZekAiViewModel(
            locationRepository = locationRepository,
            weatherRepository = weatherRepository,
            allergenRepository = allergenRepository,
            generateSuggestion = useCase,
            resourcesProvider = resources,
        )
    }

    private fun seed(suggestion: String = "wear sunscreen") {
        locationRepository.activeLocationState.value = locationEntityFixture()
        weatherRepository.currentWeatherState.value = currentWeatherFixture(name = "Istanbul", temp = 27.0)
        airQualityRepository.result = ApiResult.Success(airQualityInfoFixture())
        suggestionRepository.result = ApiResult.Success(suggestion)
    }

    @Test
    fun `no generation happens until current weather is available`() =
        runTest {
            locationRepository.activeLocationState.value = locationEntityFixture()
            // currentWeather stays null here.

            viewModel().suggestions.test {
                assertThat(awaitItem()).isNull()
                advanceUntilIdle()
                expectNoEvents()
            }
            assertThat(suggestionRepository.requests).isEmpty()
        }

    @Test
    fun `first subscription generates with the weather-derived parameters`() =
        runTest {
            seed()

            viewModel().suggestions.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo("wear sunscreen")
            }

            val request = suggestionRepository.requests.single()
            assertThat(request.location).isEqualTo("Istanbul")
            assertThat(request.temperature).isEqualTo("res:${R.string.temperature_format}:27")
            assertThat(request.forceRefresh).isFalse()
        }

    @Test
    fun `an allergen change regenerates with forceRefresh and an interim null`() =
        runTest {
            seed()

            viewModel().suggestions.test {
                skipItems(1)
                assertThat(awaitItem()).isEqualTo("wear sunscreen")

                suggestionRepository.result = ApiResult.Success("avoid the park")
                allergenRepository.prefs.value = setOf(PollenType.GRASS)

                assertThat(awaitItem()).isNull() // thinking state on a genuine input change
                assertThat(awaitItem()).isEqualTo("avoid the park")
            }
            assertThat(suggestionRepository.requests.last().forceRefresh).isTrue()
        }

    @Test
    fun `a location change regenerates without forceRefresh`() =
        runTest {
            seed()

            viewModel().suggestions.test {
                skipItems(2)
                locationRepository.activeLocationState.value =
                    locationEntityFixture(latitude = TestCoords.ANKARA_LAT, longitude = TestCoords.ANKARA_LON)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo("wear sunscreen")
            }
            assertThat(suggestionRepository.requests.last().forceRefresh).isFalse()
        }

    @Test
    fun `a failure after a prior success re-emits the last suggestion instead of clearing it`() =
        runTest {
            seed()

            viewModel().suggestions.test {
                skipItems(1)
                assertThat(awaitItem()).isEqualTo("wear sunscreen")

                suggestionRepository.result = ApiResult.Error.Network
                allergenRepository.prefs.value = setOf(PollenType.GRASS)

                assertThat(awaitItem()).isNull() // interim
                assertThat(awaitItem()).isEqualTo("wear sunscreen") // graceful fallback
            }
        }

    @Test
    fun `a failure on the very first generation leaves the thinking state`() =
        runTest {
            seed()
            suggestionRepository.result = ApiResult.Error.Network

            viewModel().suggestions.test {
                assertThat(awaitItem()).isNull()
                advanceUntilIdle()
                expectNoEvents() // lastSuggestion is null; re-emitting null conflates away
            }
        }
}
