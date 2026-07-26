package com.erendogan6.havatahminim.ui.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erendogan6.havatahminim.model.weather.hourlyforecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.currentWeatherFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.erendogan6.havatahminim.ui.viewModel.TodayViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Today screen. Assertions use fake-supplied data (place name, condition text) rather than
 * localized strings, so they hold in either locale.
 */
@RunWith(AndroidJUnit4::class)
class WeatherScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val resources = FakeResourcesProvider()

    private fun showScreen() {
        val viewModel = TodayViewModel(locationRepository, weatherRepository, resources)
        rule.setScreen {
            WeatherScreen(onUseMyLocation = {}, viewModel = viewModel)
        }
    }

    @Test
    fun currentConditionsRenderOnceTheLocationResolves() {
        weatherRepository.refreshHandler = { _, _ ->
            ApiResult.Success(currentWeatherFixture(name = "Istanbul", description = "clear sky"))
        }
        weatherRepository.hourlyHandler = { _, _ ->
            ApiResult.Success(HourlyForecastBaseResponse(listOf(currentWeatherFixture(temp = 25.0))))
        }
        locationRepository.activeLocationState.value = locationEntityFixture()

        showScreen()

        rule.awaitText("Istanbul")
        rule.onNodeWithText("Clear Sky").assertExists()
    }

    @Test
    fun aFailedRefreshShowsTheErrorMessage() {
        weatherRepository.refreshHandler = { _, _ -> ApiResult.Error.Network }
        locationRepository.activeLocationState.value = locationEntityFixture()

        showScreen()

        // FakeResourcesProvider renders ids as "res:<id>", so this asserts a message was surfaced
        // through the shared error mapping without pinning a localized string.
        rule.awaitText("res:", substring = true)
    }

    @Test
    fun theHourlyStripSurvivesAnHourlyFailure() {
        weatherRepository.refreshHandler = { _, _ ->
            ApiResult.Success(currentWeatherFixture(name = "Ankara", description = "clear sky"))
        }
        weatherRepository.hourlyHandler = { _, _ -> ApiResult.Error.Network }
        locationRepository.activeLocationState.value = locationEntityFixture()

        showScreen()

        // The conditions card is independent of the hourly slice: it still renders.
        rule.awaitText("Ankara")
    }
}
