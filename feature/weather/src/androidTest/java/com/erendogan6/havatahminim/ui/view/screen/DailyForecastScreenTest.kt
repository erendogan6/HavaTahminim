package com.erendogan6.havatahminim.ui.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erendogan6.havatahminim.model.weather.common.Weather
import com.erendogan6.havatahminim.model.weather.dailyforecast.DailyForecast
import com.erendogan6.havatahminim.model.weather.dailyforecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.dailyforecast.Temperature
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.erendogan6.havatahminim.ui.viewModel.DailyForecastViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val SECONDS_PER_DAY = 86_400L

/** Daily forecast list: keyed LazyColumn rendering and the error path. */
@RunWith(AndroidJUnit4::class)
class DailyForecastScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val resources = FakeResourcesProvider()

    private fun dayAt(
        offsetDays: Long,
        description: String,
    ) = DailyForecast(
        dt = TestTime.EPOCH_SECONDS + offsetDays * SECONDS_PER_DAY,
        sunrise = TestTime.EPOCH_SECONDS,
        sunset = TestTime.EPOCH_SECONDS,
        temp = Temperature(day = 30.0, night = 18.0),
        humidity = 40,
        weather = listOf(Weather(main = "Clear", description = description)),
    )

    private fun showScreen() {
        val viewModel = DailyForecastViewModel(locationRepository, weatherRepository, resources)
        rule.setScreen { DailyForecastScreen(viewModel = viewModel) }
    }

    @Test
    fun everyForecastDayIsRendered() {
        weatherRepository.dailyHandler = { _, _ ->
            ApiResult.Success(
                DailyForecastBaseResponse(
                    list = listOf(dayAt(0, "clear sky"), dayAt(1, "light rain"), dayAt(2, "few clouds")),
                ),
            )
        }
        locationRepository.activeLocationState.value = locationEntityFixture()

        showScreen()

        rule.awaitText("Clear Sky")
        rule.onNodeWithText("Light Rain").assertExists()
        rule.onNodeWithText("Few Clouds").assertExists()
    }

    @Test
    fun aFailedFetchShowsTheErrorMessage() {
        weatherRepository.dailyHandler = { _, _ -> ApiResult.Error.Network }
        locationRepository.activeLocationState.value = locationEntityFixture()

        showScreen()

        rule.awaitText("res:", substring = true)
    }
}
