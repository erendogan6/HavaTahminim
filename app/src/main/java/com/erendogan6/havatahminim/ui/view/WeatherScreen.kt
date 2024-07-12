package com.erendogan6.havatahminim.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.capitalizeWords
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel) {
    val weatherState = weatherViewModel.weatherState.collectAsState().value
    val errorMessage = weatherViewModel.errorMessage.collectAsState().value
    val hourlyForecast by weatherViewModel.hourlyForecast.collectAsState()
    val weatherSuggestions by weatherViewModel.weatherSuggestions.collectAsState()
    val isLoadingSuggestions = remember { mutableStateOf(true) }

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
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                            )
                        )

                        Text(
                            text = "${weatherState.main.temp.toInt()}°C",
                            fontSize = 50.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_medium_italic)),
                            modifier = Modifier.padding(vertical = 5.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                            )
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
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                            )
                        )

                        Text(
                            text = "Hissedilen Sıcaklık: ${weatherState.main.feels_like.toInt()}°C",
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            modifier = Modifier.padding(vertical = 15.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                            )
                        )
                        Text(
                            text = "Nem: ${weatherState.main.humidity}%",
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                            )
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "Saatlik Hava Durumu",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            style = TextStyle(
                                shadow = Shadow(color = Color.DarkGray, blurRadius = 1f)
                            )
                        )
                        hourlyForecast?.let {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp).clip(RoundedCornerShape(15.dp))
                                    .background(Color(0xAA80C4E9))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(it.list.take(24)) { forecast ->
                                    HourlyForecastItem(forecast)
                                }
                            }
                        }
                        weatherSuggestions?.let {
                            isLoadingSuggestions.value = false
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.zekai),
                                    contentDescription = null,
                                    modifier = Modifier.size(250.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = "ZekAI'nin Önerileri",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = TextStyle(
                                        shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(Color(0xEE000000), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        val parts = it.split("**")
                                        parts.forEachIndexed { index, part ->
                                            if (index % 2 == 1) {
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append(part)
                                                }
                                            } else {
                                                val subParts = part.split("*")
                                                subParts.forEachIndexed { subIndex, subPart ->
                                                    if (subIndex % 2 == 1) {
                                                        append("\n\t-$subPart")
                                                    } else {
                                                        append(subPart)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    fontSize = 19.sp,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.White,
                                    fontFamily = FontFamily(Font(R.font.open_sans)),
                                    style = TextStyle(
                                        shadow = Shadow(color = Color.Gray, blurRadius = 2f)
                                    )
                                )

                            }
                        } ?: run {
                            isLoadingSuggestions.value = true
                        }
                        if (isLoadingSuggestions.value) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator()
                                Text(
                                    text = "ZekAI öneriler için düşünüyor...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = TextStyle(
                                        shadow = Shadow(color = Color.DarkGray, blurRadius = 2f)
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(150.dp))
                            CircularProgressIndicator()
                            Text(
                                text = "Gökyüzü İnceleniyor...",
                                color = Color.Black,
                                fontSize = 38.sp,
                                fontFamily = FontFamily(Font(R.font.open_sans)),
                                modifier = Modifier.padding(vertical = 26.dp),
                                style = TextStyle(
                                    shadow = Shadow(color = Color.Gray, blurRadius = 2f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(forecast: CurrentWeatherBaseResponse) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = sdf.format(forecast.dt * 1000L)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            modifier = Modifier.padding(vertical = 3.dp),
            style = TextStyle(
                shadow = Shadow(color = Color.DarkGray, blurRadius = 1f)
            )
        )
        val icon = getWeatherIcon(forecast)
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(50.dp),
        )
        Text(
            text = "${forecast.main.temp.toInt()}°C",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            modifier = Modifier.padding(vertical = 3.dp),
            style = TextStyle(
                shadow = Shadow(color = Color.DarkGray, blurRadius = 1f)
            )
        )
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
