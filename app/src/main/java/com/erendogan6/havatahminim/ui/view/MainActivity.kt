package com.erendogan6.havatahminim.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HavaTahminimTheme {
                val weatherViewModel: WeatherViewModel = hiltViewModel()
                if (NetworkUtils.isNetworkAvailable(this)) {
                    weatherViewModel.fetchWeather(41.0448307, 28.9743697, "3d4e2ea2d92e6ec224c1bc97c4057c27")
                } else {
                    // Handle no internet connection
                }
                WeatherScreen(weatherViewModel)
            }
        }
    }
}
