package com.erendogan6.havatahminim.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.DailyForecastBaseResponse
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

    private val _dailyForecast = MutableStateFlow<DailyForecastBaseResponse?>(null)
    val dailyForecast: StateFlow<DailyForecastBaseResponse?> = _dailyForecast

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            handleApiCall(
                call = { repository.getWeather(lat, lon, apiKey) },
                onSuccess = { response ->
                    _weatherState.value = response
                    _errorMessage.value = null
                    fetchAdditionalData(lat, lon, apiKey, response.name, "${response.main.temp.toInt()}°C")
                    logDebug("Weather data fetched successfully", response)
                },
                onError = { handleError(it, "Error fetching weather data") }
            )
        }
    }

    private fun fetchAdditionalData(lat: Double, lon: Double, apiKey: String, location: String, temperature: String) {
        fetchHourlyForecast(lat, lon, apiKey)
        fetchDailyForecast(lat, lon, apiKey)
        fetchWeatherSuggestions(location, temperature)
    }

    private fun fetchHourlyForecast(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            handleApiCall(
                call = { repository.getHourlyWeather(lat, lon, apiKey) },
                onSuccess = { response ->
                    _hourlyForecast.value = response
                    _errorMessage.value = null
                    logDebug("Hourly forecast data fetched successfully", response)
                },
                onError = { handleError(it, "Error fetching hourly forecast data") }
            )
        }
    }

    private fun fetchWeatherSuggestions(location: String, temperature: String) {
        viewModelScope.launch {
            handleApiCall(
                call = { repository.getWeatherSuggestions(location, temperature) },
                onSuccess = { suggestions ->
                    _weatherSuggestions.value = suggestions
                    _errorMessage.value = null
                    logDebug("Weather suggestions fetched successfully", suggestions)
                },
                onError = { handleError(it, "Error fetching weather suggestions") }
            )
        }
    }

    private fun fetchDailyForecast(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            handleApiCall(
                call = { repository.getDailyWeather(lat, lon, apiKey) },
                onSuccess = { response ->
                    _dailyForecast.value = response
                    _errorMessage.value = null
                    logDebug("Daily forecast data fetched successfully", response)
                },
                onError = { handleError(it, "Error fetching daily forecast data") }
            )
        }
    }

    private suspend fun <T> handleApiCall(
        call: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val response = call()
            onSuccess(response)
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun handleError(exception: Exception, logMessage: String) {
        _errorMessage.value = "An error occurred. Please try again."
        logError(logMessage, exception)
    }

    private fun logDebug(message: String, data: Any?) {
        Log.d("WeatherViewModel", "$message: $data")
    }

    private fun logError(message: String, exception: Exception) {
        Log.e("WeatherViewModel", message, exception)
    }
}
