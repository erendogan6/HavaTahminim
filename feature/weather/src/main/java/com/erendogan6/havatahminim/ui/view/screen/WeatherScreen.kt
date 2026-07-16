package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erendogan6.havatahminim.extension.capitalizeWords
import com.erendogan6.havatahminim.extension.toHourMinute
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.weather.currentforecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.hourlyforecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.ui.adaptive.isCompactHeight
import com.erendogan6.havatahminim.ui.component.CenteredColumn
import com.erendogan6.havatahminim.ui.component.WeatherIconButton
import com.erendogan6.havatahminim.ui.component.WeatherText
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import com.erendogan6.havatahminim.ui.view.component.ErrorMessage
import com.erendogan6.havatahminim.ui.view.component.SplashScreen
import com.erendogan6.havatahminim.ui.view.component.WeatherConditionIcon
import com.erendogan6.havatahminim.ui.view.component.isDayTime
import com.erendogan6.havatahminim.ui.view.component.weatherIconRes
import com.erendogan6.havatahminim.ui.viewModel.TodayUiState
import com.erendogan6.havatahminim.ui.viewModel.TodayViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onUseMyLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCitySheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
        Box(modifier = Modifier.fillMaxSize()) {
            WeatherContent(uiState)
            // Only offer city search / my-location once data is loaded (hidden during the splash).
            if (uiState.weather != null) {
                TopActions(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp),
                    onUseMyLocation = onUseMyLocation,
                    onSearchCity = { showCitySheet = true },
                )
            }
        }
    }

    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            sheetState = sheetState,
        ) {
            CitySearchScreen(
                onCitySelected = {
                    // Play the sheet's slide-out animation before removing it from composition.
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showCitySheet = false
                    }
                },
            )
        }
    }
}

@Composable
private fun TopActions(
    modifier: Modifier = Modifier,
    onUseMyLocation: () -> Unit,
    onSearchCity: () -> Unit,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WeatherIconButton(
            icon = Icons.Default.MyLocation,
            contentDescription = stringResource(id = R.string.use_my_location),
            onClick = onUseMyLocation,
        )
        WeatherIconButton(
            icon = Icons.Default.Search,
            contentDescription = stringResource(id = R.string.select_city),
            onClick = onSearchCity,
        )
    }
}

@Composable
private fun WeatherContent(uiState: TodayUiState) {
    // Precedence: loading > error > content.
    val weather = uiState.weather
    when {
        uiState.isLoading -> SplashScreen()
        uiState.error != null -> CenteredColumn { ErrorMessage(uiState.error) }
        weather == null -> SplashScreen() // unreachable by contract; safe default
        else ->
            if (isCompactHeight()) {
                LandscapeWeatherContent(weather, uiState.hourly)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CurrentLocationCard(weather)
                    Spacer(modifier = Modifier.height(30.dp))
                    uiState.hourly?.let { HourlyForecastCard(it) }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
    }
}

/** Landscape: current conditions and the hourly forecast sit side by side, each scrolling on its own. */
@Composable
private fun LandscapeWeatherContent(
    weatherState: CurrentWeatherBaseResponse,
    hourlyForecast: HourlyForecastBaseResponse?,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CurrentLocationCard(weatherState)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            hourlyForecast?.let { HourlyForecastCard(it) }
        }
    }
}

@Composable
private fun HourlyForecastCard(hourlyForecast: HourlyForecastBaseResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        WeatherText(
            text = stringResource(id = R.string.hourly_forecast),
            style = MaterialTheme.typography.headlineSmall
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(WeatherTheme.colors.cardSurface)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(hourlyForecast.list.take(24)) { forecast ->
                HourlyForecastItem(forecast)
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(weatherState: CurrentWeatherBaseResponse) {
    // One TalkBack node for the whole block instead of five separate stops.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        CurrentLocationCardContent(weatherState)
    }
}

@Composable
private fun CurrentLocationCardContent(weatherState: CurrentWeatherBaseResponse) {
    Spacer(modifier = Modifier.height(24.dp))

    WeatherText(
        text = weatherState.name,
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier.padding(vertical = 12.dp),
    )

    WeatherText(
        text = stringResource(id = R.string.temperature_format, weatherState.main.temp.toInt()),
        style = MaterialTheme.typography.displayLarge,
        modifier = Modifier.padding(vertical = 5.dp),
    )

    WeatherConditionIcon(
        main = weatherState.weather[0].main,
        isDayTime = weatherState.isDayTime(),
        modifier = Modifier.size(150.dp),
    )

    WeatherText(
        text = weatherState.weather[0].description.capitalizeWords(),
        style = MaterialTheme.typography.headlineMedium,
    )

    WeatherText(
        text = stringResource(id = R.string.feels_like_format, weatherState.main.feelsLike.toInt()),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(vertical = 15.dp),
    )

    WeatherText(
        text = stringResource(id = R.string.humidity_format, weatherState.main.humidity),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun HourlyForecastItem(forecast: CurrentWeatherBaseResponse) {
    val locale = LocalConfiguration.current.locales[0]
    val date = forecast.dt.toHourMinute(locale)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        // TalkBack reads each hour as one node.
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        WeatherText(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        Image(
            painter = painterResource(id = weatherIconRes(forecast.weather[0].main, forecast.isDayTime())),
            // The icon is the only carrier of the condition here, so it gets a description.
            contentDescription = forecast.weather[0].description,
            modifier = Modifier.size(60.dp),
        )
        WeatherText(
            text = stringResource(id = R.string.temperature_format, forecast.main.temp.toInt()),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        forecast.pop?.let { pop ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = stringResource(id = R.string.a11y_precipitation_probability),
                    tint = WeatherTheme.colors.precipitation,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.size(2.dp))
                WeatherText(
                    text = stringResource(id = R.string.pop_format, pop),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = WeatherTheme.colors.precipitation,
                )
            }
        }
    }
}
