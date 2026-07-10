package com.erendogan6.havatahminim.ui.viewModel

import app.cash.turbine.test
import com.erendogan6.havatahminim.testing.fixture.currentWeatherFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()

    private fun viewModel() = MainViewModel(locationRepository, weatherRepository)

    @Test
    fun `currentWeather mirrors the repository's shared state`() =
        runTest {
            val weather = currentWeatherFixture()
            val viewModel = viewModel()

            viewModel.currentWeather.test {
                assertThat(awaitItem()).isNull()
                weatherRepository.currentWeatherState.value = weather
                assertThat(awaitItem()).isEqualTo(weather)
            }
        }

    @Test
    fun `setLocation persists the new location`() =
        runTest {
            viewModel().setLocation(1.0, 2.0)
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls).containsExactly(Triple(1.0, 2.0, true))
        }

    @Test
    fun `startFromSavedOrDefault uses the saved location when present`() =
        runTest {
            locationRepository.savedLocation = locationEntityFixture()

            viewModel().startFromSavedOrDefault()
            advanceUntilIdle()

            assertThat(locationRepository.activeLocation.value).isEqualTo(locationEntityFixture())
            assertThat(locationRepository.setActiveLocationCalls).isEmpty() // no fallback fired
        }

    @Test
    fun `startFromSavedOrDefault falls back to Istanbul without persisting`() =
        runTest {
            locationRepository.savedLocation = null

            viewModel().startFromSavedOrDefault()
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls)
                .containsExactly(Triple(41.0082, 28.9784, false))
        }
}
