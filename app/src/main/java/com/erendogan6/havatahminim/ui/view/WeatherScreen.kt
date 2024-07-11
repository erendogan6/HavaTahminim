package com.erendogan6.havatahminim.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.BaseResponse
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.capitalizeWords
import java.util.*

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel) {
    val weatherState = weatherViewModel.weatherState.collectAsState().value
    val errorMessage = weatherViewModel.errorMessage.collectAsState().value

    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.aydinlik),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.6f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    errorMessage != null -> {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                    weatherState != null -> {
                        val weatherIcon = getWeatherIcon(weatherState)

                        Text(
                            text = weatherState.name,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = "${weatherState.main.temp}°C",
                            fontSize = 35.sp,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )

                        Image(
                            painter = weatherIcon,
                            contentDescription = null,
                            modifier = Modifier.size(135.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = weatherState.weather[0].description.capitalizeWords(),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Hissedilen Sıcaklık: ${weatherState.main.feels_like}°C",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 15.dp)
                        )
                        Text(
                            text = "Nem: ${weatherState.main.humidity}%",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(48.dp))
                            CircularProgressIndicator()
                            Text(
                                text = "Gökyüzü İnceleniyor...",
                                fontSize = 26.sp,
                                modifier = Modifier.padding(vertical = 26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getWeatherIcon(weatherResponse: BaseResponse): Painter {
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
