package com.erendogan6.havatahminim.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel) {
    val weatherState = weatherViewModel.weatherState.collectAsState().value

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            weatherState?.let {
                Text(text = "Durum: ${it.weather[0].description}")
                Text(text = "Sıcaklık: ${it.main.temp}°C")
                Text(text = "Hissedilen Sıcaklık: ${it.main.feels_like}°C")
                Text(text = "Nem: ${it.main.humidity}%")
            } ?: run {
                Text(text = "Veri yükleniyor...")
            }
        }
    }
}
