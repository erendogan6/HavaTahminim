package com.erendogan6.havatahminim.ui.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.NetworkUtils
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HavaTahminimApp()
        }
    }
}

@Composable
fun HavaTahminimApp() {
    HavaTahminimTheme {
        val weatherViewModel: WeatherViewModel = hiltViewModel()
        val context = LocalContext.current
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        var locationPermissionGranted by remember { mutableStateOf(false) }
        var latitude by remember { mutableDoubleStateOf(41.0448307) }
        var longitude by remember { mutableDoubleStateOf(28.9743697) }

        RequestLocationPermission(
            onPermissionGranted = { locationPermissionGranted = true },
            onPermissionDenied = { locationPermissionGranted = false }
        )

        if (locationPermissionGranted) {
            GetCurrentLocation(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onNewLocation = { lat, lon ->
                    latitude = lat
                    longitude = lon
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        weatherViewModel.fetchWeather(lat, lon, "3d4e2ea2d92e6ec224c1bc97c4057c27")
                    } else {
                        // Handle no internet connection
                    }
                }
            )
        } else {
            // Handle permission not granted
        }

        WeatherScreen(weatherViewModel)
    }
}

@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@Composable
fun GetCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onNewLocation: (Double, Double) -> Unit
) {
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            try {
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                location?.let {
                    onNewLocation(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }
}
