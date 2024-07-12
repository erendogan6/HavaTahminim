package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<CurrentWeatherBaseResponse?>(null)
    val weatherState: StateFlow<CurrentWeatherBaseResponse?> = _weatherState

    private val _hourlyForecast = MutableStateFlow<HourlyForecastBaseResponse?>(null)
    val hourlyForecast: StateFlow<HourlyForecastBaseResponse?> = _hourlyForecast

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _weatherSuggestions = MutableStateFlow<String?>(null)
    val weatherSuggestions: StateFlow<String?> = _weatherSuggestions

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.getWeather(lat, lon, apiKey)
                _weatherState.value = response
                _errorMessage.value = null

                fetchHourlyForecast(lat, lon, apiKey)

                fetchWeatherSuggestions(response.name, "${response.main.temp.toInt()}°C")

                Log.d("WeatherViewModel", "Weather data fetched successfully")
            } catch (e: Exception) {
                _errorMessage.value = "Veri yüklenemedi. Lütfen tekrar deneyin."
                Log.e("WeatherViewModel", "Error fetching weather data", e)
            }
        }
    }

    private fun fetchHourlyForecast(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.getHourlyWeather(lat, lon, apiKey)
                _hourlyForecast.value = response
                _errorMessage.value = null
                Log.d("WeatherViewModel", "Hourly forecast data fetched successfully")
            } catch (e: Exception) {
                _errorMessage.value = "Saatlik tahminler yüklenemedi. Lütfen tekrar deneyin."
                Log.e("WeatherViewModel", "Error fetching hourly forecast data", e)
            }
        }
    }

    private fun fetchWeatherSuggestions(location: String, temperature: String) {
        viewModelScope.launch {
            try {
                val suggestions = repository.getWeatherSuggestions(location, temperature)
                _weatherSuggestions.value = suggestions
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
