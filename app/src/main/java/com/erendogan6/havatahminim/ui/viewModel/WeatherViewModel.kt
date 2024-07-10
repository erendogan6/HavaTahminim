package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.erendogan6.havatahminim.model.BaseResponse
import com.erendogan6.havatahminim.repository.WeatherRepository

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<BaseResponse?>(null)
    val weatherState: StateFlow<BaseResponse?> = _weatherState

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.getWeather(lat, lon, apiKey)
                _weatherState.value = response
                Log.d("WeatherViewModel", "Weather data fetched successfully")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather data", e)
            }
        }
    }
}