package com.erendogan6.havatahminim.ui.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erendogan6.havatahminim.domain.GenerateWeatherSuggestionUseCase
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.fixture.currentWeatherFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeAirQualityRepository
import com.erendogan6.havatahminim.testing.repository.FakeAllergenRepository
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeSuggestionRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.erendogan6.havatahminim.ui.viewModel.ZekAiViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ZekAI screen. The suggestion body renders through MarkdownText (an AndroidView), so the
 * assertions target the Compose-side card chrome rather than the markdown text itself.
 */
@RunWith(AndroidJUnit4::class)
class ZekAiScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val allergenRepository = FakeAllergenRepository()
    private val airQualityRepository = FakeAirQualityRepository()
    private val suggestionRepository = FakeSuggestionRepository()
    private val resources = FakeResourcesProvider()

    private fun showScreen() {
        val useCase =
            GenerateWeatherSuggestionUseCase(
                airQualityRepository,
                suggestionRepository,
                resources,
                TestTime.clock(),
            )
        val viewModel =
            ZekAiViewModel(
                locationRepository,
                weatherRepository,
                allergenRepository,
                useCase,
                resources,
            )
        rule.setScreen { ZekAIScreen(viewModel = viewModel) }
    }

    private fun seedWeather() {
        weatherRepository.currentWeatherState.value = currentWeatherFixture(name = "Istanbul")
        locationRepository.activeLocationState.value = locationEntityFixture()
    }

    @Test
    fun theSuggestionCardReplacesTheThinkingStateOnceGenerated() {
        suggestionRepository.result = ApiResult.Success("Bugun yuruyus icin harika bir gun.")
        seedWeather()

        showScreen()

        rule.awaitText(rule.activity.getString(R.string.zekai_suggestions))
    }

    @Test
    fun aFailedGenerationDoesNotShowTheSuggestionCard() {
        suggestionRepository.result = ApiResult.Error.Network
        seedWeather()

        showScreen()
        rule.waitForIdle()

        rule.awaitNoText(rule.activity.getString(R.string.zekai_suggestions))
    }
}
