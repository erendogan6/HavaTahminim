package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.extension.capitalizeWords
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecast
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DailyForecastScreen(
    weatherViewModel: WeatherViewModel,
    onLoaded: () -> Unit,
) {
    val dailyForecast by weatherViewModel.dailyForecast.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()

    WeatherBackgroundLayout(weatherState) {
        Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
            dailyForecast?.let {
                onLoaded()
                DailyForecastCard(it)
            } ?: SplashScreen()
        }
    }
}

@Composable
fun DailyForecastCard(dailyForecast: DailyForecastBaseResponse) {
    val dayNames =
        dailyForecast.list.map {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(it.dt * 1000L)
        }
    val maxWidth = dayNames.maxOfOrNull { it.length } ?: 0

    Spacer(modifier = Modifier.size(30.dp))
    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.daily_forecast_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            style = TextStyle(shadow = Shadow(color = Color.Gray, blurRadius = 1f)),
        )
        Spacer(modifier = Modifier.size(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(dailyForecast.list) { forecast ->
                DailyForecastItem(forecast, maxWidth * 10f)
            }
        }
    }
}

@Composable
fun DailyForecastItem(
    forecast: DailyForecast,
    maxWidth: Float,
) {
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val day = dayFormat.format(forecast.dt * 1000L)

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xAA80C4E9),
            ),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Text(
                text = day,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.open_sans)),
                modifier = Modifier.widthIn(min = maxWidth.dp),
                style =
                    TextStyle(
                        shadow = Shadow(color = Color.DarkGray, blurRadius = 1f),
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f).padding(end = 10.dp),
            ) {
                val icon = getWeatherIconn(forecast.weather[0].main)
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                )
                Text(
                    text = forecast.weather[0].description.capitalizeWords(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily(Font(R.font.open_sans)),
                    modifier = Modifier.padding(vertical = 3.dp),
                    style =
                        TextStyle(
                            shadow = Shadow(color = Color.DarkGray, blurRadius = 1f),
                        ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = "${forecast.temp.day.toInt()}°C",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.open_sans)),
                modifier =
                    Modifier
                        .weight(0.5f)
                        .padding(start = 10.dp, end = 10.dp),
                style =
                    TextStyle(
                        shadow = Shadow(color = Color.DarkGray, blurRadius = 1f),
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun getWeatherIconn(main: String): Painter {
    val resourceId =
        when (main) {
            "Clouds" -> R.drawable.day_partial_cloud
            "Clear" -> R.drawable.day_clear
            "Snow" -> R.drawable.day_snow
            "Rain" -> R.drawable.day_rain
            "Drizzle" -> R.drawable.day_rain
            "Thunderstorm" -> R.drawable.day_rain_thunder
            "Fog" -> R.drawable.fog
            "Mist" -> R.drawable.mist
            else -> R.drawable.cloudy
        }
    return painterResource(id = resourceId)
}
