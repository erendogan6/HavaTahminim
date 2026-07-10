package com.erendogan6.havatahminim.ui.viewModel

import app.cash.turbine.test
import com.erendogan6.havatahminim.core.data.R as DataR
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DailyForecastViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val resources = FakeResourcesProvider()

    private val forecast = DailyForecastBaseResponse(list = emptyList())

    private fun viewModel() = DailyForecastViewModel(locationRepository, weatherRepository, resources)

    @Test
    fun `happy path emits Loading then Success`() =
        runTest {
            weatherRepository.dailyHandler = { _, _ -> ApiResult.Success(forecast) }
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                assertThat(awaitItem()).isEqualTo(DailyUiState.Loading)
                assertThat(awaitItem()).isEqualTo(DailyUiState.Success(forecast))
            }
        }

    @Test
    fun `failure surfaces the localized error`() =
        runTest {
            weatherRepository.dailyHandler = { _, _ -> ApiResult.Error.Http(500) }
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().uiState.test {
                assertThat(awaitItem()).isEqualTo(DailyUiState.Loading)
                assertThat(awaitItem())
                    .isEqualTo(DailyUiState.Error("res:${DataR.string.error_fetching_daily_forecast}"))
            }
        }

    @Test
    fun `location change re-fetches with Loading, resubscribe does not`() =
        runTest {
            weatherRepository.dailyHandler = { _, _ -> ApiResult.Success(forecast) }
            locationRepository.activeLocationState.value = locationEntityFixture()
            val viewModel = viewModel()

            viewModel.uiState.test {
                skipItems(2) // Loading + Success
                locationRepository.activeLocationState.value =
                    locationEntityFixture(latitude = TestCoords.ANKARA_LAT, longitude = TestCoords.ANKARA_LON)
                assertThat(awaitItem()).isEqualTo(DailyUiState.Loading) // genuine input change
                assertThat(awaitItem()).isEqualTo(DailyUiState.Success(forecast))
            }

            advanceTimeBy(5_001)
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(DailyUiState.Success(forecast)) // replay, no Loading
                advanceUntilIdle() // let the silent background re-fetch complete
                expectNoEvents() // same data — nothing new emitted
            }
            assertThat(weatherRepository.dailyCallCount).isEqualTo(3) // initial + change + silent refresh
        }
}
