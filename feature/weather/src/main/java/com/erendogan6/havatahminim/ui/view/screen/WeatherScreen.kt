package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.erendogan6.havatahminim.ui.component.WeatherIconButton
import com.erendogan6.havatahminim.ui.component.WeatherText
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.extension.capitalizeWords
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.ui.adaptive.isCompactHeight
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel,
    onLoaded: () -> Unit,
    onUseMyLocation: () -> Unit,
) {
    val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
    val errorMessage by weatherViewModel.errorMessage.collectAsStateWithLifecycle()
    val hourlyForecast by weatherViewModel.hourlyForecast.collectAsStateWithLifecycle()
    var showCitySheet by rememberSaveable { mutableStateOf(false) }

    WeatherBackgroundLayout(weatherState) {
        Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
            Box(modifier = Modifier.fillMaxSize()) {
                WeatherContent(weatherState, errorMessage, hourlyForecast, onLoaded = onLoaded)
                // Only offer city search / my-location once data is loaded (hidden during the splash).
                if (weatherState != null) {
                    TopActions(
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp),
                        onUseMyLocation = onUseMyLocation,
                        onSearchCity = { showCitySheet = true },
                    )
                }
            }
        }
    }

    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            CitySearchScreen(weatherViewModel) { city ->
                weatherViewModel.updateLocationAndFetchWeather(city.latitude, city.longitude)
                showCitySheet = false
            }
        }
    }
}

@Composable
private fun TopActions(
    modifier: Modifier,
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
fun WeatherBackgroundLayout(
    weatherState: CurrentWeatherBaseResponse?,
    content: @Composable () -> Unit
) {
    // The weather background is now drawn once, full-bleed, behind the Scaffold in MainActivity
    // (so it extends edge-to-edge under the system bars). This stays a transparent passthrough.
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}

@Composable
fun BackgroundImage(weatherState: CurrentWeatherBaseResponse?) {
    val backgroundImage = if (weatherState != null) R.drawable.aydinlik else R.drawable.splash
    val alpha = if (weatherState != null) 0.5f else 0.7f

    Image(
        painter = painterResource(id = backgroundImage),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        alpha = alpha
    )
}

@Composable
fun WeatherContent(weatherState: CurrentWeatherBaseResponse?,
                   errorMessage: String?,
                   hourlyForecast: HourlyForecastBaseResponse?,
                   onLoaded: () -> Unit
) {
    when {
        errorMessage != null ->
            CenteredColumn { ErrorMessage(errorMessage) }
        weatherState != null -> {
            onLoaded()
            if (isCompactHeight()) {
                LandscapeWeatherContent(weatherState, hourlyForecast)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CurrentLocationCard(weatherState)
                    Spacer(modifier = Modifier.height(30.dp))
                    hourlyForecast?.let { HourlyForecastCard(it) }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        else -> SplashScreen()
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CurrentLocationCard(weatherState)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            hourlyForecast?.let { HourlyForecastCard(it) }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    WeatherText(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(vertical = 20.dp)
    )
}

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 9000, easing = LinearEasing)),
        label = "rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    // Compact height (landscape): shrink the halo/icon and spacings so the splash fits
    // without clipping.
    val compact = isCompactHeight()
    val glow = WeatherTheme.colors.glow
    CenteredColumn {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (compact) 132.dp else 200.dp)) {
            // Soft glowing halo that breathes behind the icon.
            Box(
                modifier = Modifier
                    .size(if (compact) 118.dp else 180.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(glow.copy(alpha = 0.53f), glow.copy(alpha = 0.2f), glow.copy(alpha = 0f))
                        )
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.day_clear),
                contentDescription = null,
                modifier = Modifier
                    .size(if (compact) 80.dp else 120.dp)
                    .rotate(rotation),
            )
        }
        Spacer(modifier = Modifier.height(if (compact) 12.dp else 28.dp))
        WeatherText(
            text = stringResource(id = R.string.loading_message),
            color = WeatherTheme.colors.ink,
            style =
                (if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge)
                    .copy(shadow = Shadow(color = glow, blurRadius = 8f)),
        )
        Spacer(modifier = Modifier.height(if (compact) 8.dp else 18.dp))
        LoadingDots()
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val ink = WeatherTheme.colors.ink
    Row {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(durationMillis = 600, delayMillis = index * 180, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(ink.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun HourlyForecastCard(hourlyForecast: HourlyForecastBaseResponse) {
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
fun CurrentLocationCard(weatherState: CurrentWeatherBaseResponse) {
    val weatherIcon = getWeatherIcon(weatherState)

    Spacer(modifier = Modifier.height(24.dp))

    WeatherText(
        text = weatherState.name,
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier.padding(vertical = 12.dp),
    )

    WeatherText(
        text = "${weatherState.main.temp.toInt()}°C",
        style = MaterialTheme.typography.displayLarge,
        modifier = Modifier.padding(vertical = 5.dp),
    )

    Image(
        painter = weatherIcon,
        contentDescription = null,
        modifier = Modifier.size(150.dp)
    )

    WeatherText(
        text = weatherState.weather[0].description.capitalizeWords(),
        style = MaterialTheme.typography.headlineMedium,
    )

    WeatherText(
        text = "${stringResource(id = R.string.feels_like)}: ${weatherState.main.feels_like.toInt()}°C",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(vertical = 15.dp),
    )

    WeatherText(
        text = "${stringResource(id = R.string.humidity)}: ${weatherState.main.humidity}%",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}



@Composable
fun HourlyForecastItem(forecast: CurrentWeatherBaseResponse) {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = SimpleDateFormat("HH:mm", locale)
    val date = sdf.format(forecast.dt * 1000L)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WeatherText(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        val icon = getWeatherIcon(forecast)
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
        )
        WeatherText(
            text = "${forecast.main.temp.toInt()}°C",
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
                    contentDescription = null,
                    tint = WeatherTheme.colors.precipitation,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.size(2.dp))
                WeatherText(
                    text = "%$pop",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = WeatherTheme.colors.precipitation,
                )
            }
        }
    }
}

@Composable
fun getWeatherIcon(weatherResponse: CurrentWeatherBaseResponse): Painter {
    val weatherMain = weatherResponse.weather[0].main
    val currentTime = Calendar.getInstance().timeInMillis / 1000
    val isDayTime = currentTime in (weatherResponse.sys.sunrise..weatherResponse.sys.sunset)

    val resourceId = when (weatherMain) {
        "Clouds" -> if (isDayTime) R.drawable.day_partial_cloud else R.drawable.night_half_moon_partial_cloud
        "Clear" -> if (isDayTime) R.drawable.day_clear else R.drawable.night_half_moon_clear
        "Snow" -> if (isDayTime) R.drawable.day_snow else R.drawable.night_half_moon_snow
        "Rain" -> if (isDayTime) R.drawable.day_rain else R.drawable.night_half_moon_rain
        "Drizzle" -> if (isDayTime) R.drawable.day_rain else R.drawable.night_half_moon_rain
        "Thunderstorm" -> if (isDayTime) R.drawable.day_rain_thunder else R.drawable.night_half_moon_rain_thunder
        "Fog" -> R.drawable.fog
        "Mist" -> R.drawable.mist
        else -> R.drawable.cloudy
    }

    return painterResource(id = resourceId)
}

@Composable
fun CenteredColumn(content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}
