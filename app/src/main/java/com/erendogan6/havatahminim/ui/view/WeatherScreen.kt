package com.erendogan6.havatahminim.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.BaseResponse
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.capitalizeWords
import java.util.Calendar


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
                painter = if (weatherState != null) {
                    painterResource(id = R.drawable.aydinlik)
                } else {
                    painterResource(id = R.drawable.splash)
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = if (weatherState != null) {
                    0.5f
                } else {
                    0.7f
                }
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

                        Spacer(modifier = Modifier.height(24.dp))


                        Text(
                            text = weatherState.name,
                                fontSize = 36.sp,
                                    fontFamily = FontFamily(Font(R.font.merriweather)),
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    style = TextStyle(
                                        shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)))

                        Text(
                            text = "${weatherState.main.temp}°C",
                            fontSize = 50.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_medium_italic)),
                            modifier = Modifier.padding(vertical = 5.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
                        )

                        Image(
                            painter = weatherIcon,
                            contentDescription = null,
                            modifier = Modifier.size(150.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = weatherState.weather[0].description.capitalizeWords(),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
                        )

                        Text(
                            text = "Hissedilen Sıcaklık: ${weatherState.main.feels_like}°C",
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            modifier = Modifier.padding(vertical = 15.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
                        )
                        Text(
                            text = "Nem: ${weatherState.main.humidity}%",
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(120.dp))
                            CircularProgressIndicator()
                            Text(
                                text = "Gökyüzü İnceleniyor...",
                                fontSize = 28.sp,
                                fontFamily = FontFamily(Font(R.font.open_sans)),
                                modifier = Modifier.padding(vertical = 26.dp),
                                style = TextStyle(
                                    shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
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
