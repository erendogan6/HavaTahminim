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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.erendogan6.havatahminim.ui.component.WeatherCard
import com.erendogan6.havatahminim.ui.component.WeatherText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.extension.capitalizeWords
import com.erendogan6.havatahminim.extension.toDayName
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecast
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import com.erendogan6.havatahminim.ui.view.component.SplashScreen
import com.erendogan6.havatahminim.ui.view.component.weatherIconRes
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel

@Composable
fun DailyForecastScreen(
    weatherViewModel: WeatherViewModel,
    onLoaded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dailyForecast by weatherViewModel.dailyForecast.collectAsStateWithLifecycle()

    // See WeatherContent: notify data-arrival after composition, always via the latest callback.
    val currentOnLoaded by rememberUpdatedState(onLoaded)
    val hasData = dailyForecast != null
    LaunchedEffect(hasData) {
        if (hasData) currentOnLoaded()
    }

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
        dailyForecast?.let { DailyForecastCard(it) } ?: SplashScreen()
    }
}

@Composable
private fun DailyForecastCard(dailyForecast: DailyForecastBaseResponse) {
    val locale = LocalConfiguration.current.locales[0]
    val dayNames = dailyForecast.list.map { it.dt.toDayName(locale) }
    val maxWidth = dayNames.maxOfOrNull { it.length } ?: 0

    Spacer(modifier = Modifier.size(30.dp))
    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WeatherText(
            text = stringResource(id = R.string.daily_forecast_title),
            style = MaterialTheme.typography.headlineLarge,
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
private fun DailyForecastItem(
    forecast: DailyForecast,
    maxWidth: Float,
) {
    val locale = LocalConfiguration.current.locales[0]
    val day = forecast.dt.toDayName(locale)

    WeatherCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        ) {
            WeatherText(
                text = day,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = maxWidth.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f).padding(end = 10.dp),
            ) {
                Image(
                    painter = painterResource(id = weatherIconRes(forecast.weather[0].main)),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                )
                WeatherText(
                    text = forecast.weather[0].description.capitalizeWords(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 3.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            WeatherText(
                text = "${forecast.temp.day.toInt()}°C",
                style = MaterialTheme.typography.headlineSmall,
                modifier =
                    Modifier
                        .weight(0.5f)
                        .padding(start = 10.dp, end = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

