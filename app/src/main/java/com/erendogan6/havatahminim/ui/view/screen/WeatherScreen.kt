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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.extension.capitalizeWords
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
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
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val errorMessage by weatherViewModel.errorMessage.collectAsState()
    val hourlyForecast by weatherViewModel.hourlyForecast.collectAsState()
    var showCitySheet by remember { mutableStateOf(false) }

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
        ActionIconButton(Icons.Default.MyLocation, R.string.use_my_location, onUseMyLocation)
        ActionIconButton(Icons.Default.Search, R.string.select_city, onSearchCity)
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    descRes: Int,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0x66FFFFFF)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = descRes),
            tint = Color(0xFF1B3A4B),
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            errorMessage != null -> ErrorMessage(errorMessage)
            weatherState != null -> {
                onLoaded()
                CurrentLocationCard(weatherState)
                Spacer(modifier = Modifier.height(30.dp))
                hourlyForecast?.let { HourlyForecastCard(it) }
                Spacer(modifier = Modifier.height(16.dp))
            }
            else -> SplashScreen()
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
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

    CenteredColumn {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            // Soft glowing halo that breathes behind the icon.
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x88FFFFFF), Color(0x33FFFFFF), Color(0x00FFFFFF))
                        )
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.day_clear),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .rotate(rotation),
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(id = R.string.loading_message),
            color = Color(0xFF1B3A4B),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            style = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 8f))
        )
        Spacer(modifier = Modifier.height(18.dp))
        LoadingDots()
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
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
                    .background(Color(0xFF1B3A4B).copy(alpha = alpha))
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
        Text(
            text = stringResource(id = R.string.hourly_forecast),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 1f))
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xAA80C4E9))
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

    Text(
        text = weatherState.name,
        fontSize = 36.sp,
        fontFamily = FontFamily(Font(R.font.merriweather)),
        modifier = Modifier.padding(vertical = 12.dp),
        style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
    )

    Text(
        text = "${weatherState.main.temp.toInt()}°C",
        fontSize = 50.sp,
        fontFamily = FontFamily(Font(R.font.roboto_medium_italic)),
        modifier = Modifier.padding(vertical = 5.dp),
        style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
    )

    Image(
        painter = weatherIcon,
        contentDescription = null,
        modifier = Modifier.size(150.dp)
    )

    Text(
        text = weatherState.weather[0].description.capitalizeWords(),
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(Font(R.font.open_sans)),
        style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
    )

    Text(
        text = "${stringResource(id = R.string.feels_like)}: ${weatherState.main.feels_like.toInt()}°C",
        fontSize = 22.sp,
        fontFamily = FontFamily(Font(R.font.open_sans)),
        modifier = Modifier.padding(vertical = 15.dp),
        style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
    )

    Text(
        text = "${stringResource(id = R.string.humidity)}: ${weatherState.main.humidity}%",
        fontSize = 22.sp,
        fontFamily = FontFamily(Font(R.font.open_sans)),
        modifier = Modifier.padding(vertical = 4.dp),
        style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
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
        Text(
            text = date,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            modifier = Modifier.padding(vertical = 6.dp),
            style = TextStyle(
                shadow = Shadow(color = Color.DarkGray, blurRadius = 1f)
            )
        )
        val icon = getWeatherIcon(forecast)
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
        )
        Text(
            text = "${forecast.main.temp.toInt()}°C",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            modifier = Modifier.padding(top = 6.dp),
            style = TextStyle(
                shadow = Shadow(color = Color.DarkGray, blurRadius = 1f)
            )
        )
        forecast.pop?.let { pop ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = "%$pop",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    fontFamily = FontFamily(Font(R.font.open_sans)),
                    style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 1f))
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
