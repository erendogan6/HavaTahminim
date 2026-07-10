package com.erendogan6.havatahminim.ui.viewModel

import app.cash.turbine.test
import com.erendogan6.havatahminim.core.data.R as DataR
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.currentWeatherFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TodayViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val resources = FakeResourcesProvider()

    private val weather = currentWeatherFixture()
    private val hourly = HourlyForecastBaseResponse(listOf(currentWeatherFixture(temp = 25.0)))

    private fun viewModel() = TodayViewModel(locationRepository, weatherRepository, resources)

    private fun seedHappyPath() {
        weatherRepository.refreshHandler = { _, _ -> ApiResult.Success(weather) }
        weatherRepository.hourlyHandler = { _, _ -> ApiResult.Success(hourly) }
    }

    @Test
    fun `happy path emits Loading, then weather, then weather with hourly`() =
        runTest {
            seedHappyPath()
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                assertThat(awaitItem()).isEqualTo(TodayUiState.Loading)
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = null))
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = hourly))
            }
        }

    @Test
    fun `hourly failure keeps the weather success without an error state`() =
        runTest {
            weatherRepository.refreshHandler = { _, _ -> ApiResult.Success(weather) }
            weatherRepository.hourlyHandler = { _, _ -> ApiResult.Error.Network }
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                assertThat(awaitItem()).isEqualTo(TodayUiState.Loading)
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = null))
                expectNoEvents()
            }
        }

    @Test
    fun `refresh failure surfaces the localized error`() =
        runTest {
            weatherRepository.refreshHandler = { _, _ -> ApiResult.Error.Network }
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                assertThat(awaitItem()).isEqualTo(TodayUiState.Loading)
                assertThat(awaitItem())
                    .isEqualTo(TodayUiState.Error("res:${DataR.string.error_fetching_weather_data}"))
            }
        }

    @Test
    fun `a location change cancels the stale fetch — only the new location lands`() =
        runTest {
            weatherRepository.refreshHandler = { _, _ -> awaitCancellation() } // first fetch hangs
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                assertThat(awaitItem()).isEqualTo(TodayUiState.Loading)
                advanceUntilIdle() // let the first fetch actually start (and hang in awaitCancellation)

                seedHappyPath() // second location resolves normally
                locationRepository.activeLocationState.value =
                    locationEntityFixture(latitude = TestCoords.ANKARA_LAT, longitude = TestCoords.ANKARA_LON)

                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = null))
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = hourly))
            }
            assertThat(weatherRepository.refreshCallCount).isEqualTo(2)
        }

    @Test
    fun `resubscribing after teardown refreshes silently without a Loading flash`() =
        runTest {
            seedHappyPath()
            locationRepository.activeLocationState.value = locationEntityFixture()
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(TodayUiState.Loading)
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = null))
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = hourly))
            }

            advanceTimeBy(5_001) // let WhileSubscribed(5s) tear the upstream down

            viewModel.uiState.test {
                // stateIn replays the last value; the silent re-fetch must not emit Loading.
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = hourly))
                advanceUntilIdle()
                expectMostRecentItem().let { assertThat(it).isInstanceOf(TodayUiState.Success::class.java) }
            }
            assertThat(weatherRepository.refreshCallCount).isEqualTo(2)
        }

    @Test
    fun `re-emitting the same coordinates does not restart the pipeline`() =
        runTest {
            seedHappyPath()
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                skipItems(3) // Loading + two Successes
                locationRepository.activeLocationState.value = locationEntityFixture() // same coords, new instance
                advanceUntilIdle()
                expectNoEvents()
            }
            assertThat(weatherRepository.refreshCallCount).isEqualTo(1)
        }

    @Test
    fun `after an error the next subscription shows Loading again`() =
        runTest {
            weatherRepository.refreshHandler = { _, _ -> ApiResult.Error.Network }
            locationRepository.activeLocationState.value = locationEntityFixture()
            val viewModel = viewModel()

            viewModel.uiState.test {
                skipItems(1) // Loading
                assertThat(awaitItem()).isInstanceOf(TodayUiState.Error::class.java)
            }
            advanceTimeBy(5_001)
            seedHappyPath()

            viewModel.uiState.test {
                skipItems(1) // replayed Error
                // lastCoords was reset on failure, so the retry legitimately re-shows Loading.
                assertThat(awaitItem()).isEqualTo(TodayUiState.Loading)
                assertThat(awaitItem()).isEqualTo(TodayUiState.Success(weather, hourly = null))
            }
        }
}
