package com.erendogan6.havatahminim.ui.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.view.navigation.BottomNavigationBar
import com.erendogan6.havatahminim.ui.view.navigation.Screen
import com.erendogan6.havatahminim.ui.view.screen.CitySearchScreen
import com.erendogan6.havatahminim.ui.view.screen.DailyForecastScreen
import com.erendogan6.havatahminim.ui.view.screen.WeatherScreen
import com.erendogan6.havatahminim.ui.view.screen.ZekAIScreen
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.NetworkUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
        val coroutineScope = rememberCoroutineScope()
        var locationPermissionGranted by remember { mutableStateOf(false) }
        var showPermissionRationale by remember { mutableStateOf(false) }
        var locationError by remember { mutableStateOf<String?>(null) }
        val navController = rememberNavController()
        var dataLoaded by remember { mutableStateOf(false) }
        val savedLocation by weatherViewModel.location.collectAsState()

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            locationPermissionGranted = isGranted
            showPermissionRationale = !isGranted
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        LaunchedEffect(locationPermissionGranted) {
            if (locationPermissionGranted) {
                coroutineScope.launch {
                    getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            weatherViewModel.saveLocation(lat, lon)
                            if (!dataLoaded) {
                                weatherViewModel.fetchWeather(lat, lon)
                                dataLoaded = true
                            }
                        } else {
                            locationError = context.getString(R.string.no_internet)
                        }
                    }.onFailure {
                        locationError = it.message
                    }
                }
            } else {
                savedLocation?.let {
                    if (!dataLoaded && NetworkUtils.isNetworkAvailable(context)) {
                        weatherViewModel.fetchWeather(it.latitude, it.longitude)
                        dataLoaded = true
                    } else if (!dataLoaded) {
                        // İstanbul koordinatları
                        val defaultLat = 41.0082
                        val defaultLon = 28.9784
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            weatherViewModel.fetchWeather(defaultLat, defaultLon)
                            dataLoaded = true
                        } else {
                            locationError = context.getString(R.string.no_internet)
                        }
                    }
                }
            }
        }

        if (showPermissionRationale && !locationPermissionGranted) {
            PermissionRationaleDialog(
                onDismiss = { showPermissionRationale = false },
                onRequestPermission = {
                    showPermissionRationale = false
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            )
        }

        locationError?.let {
            ErrorDialog(
                message = it,
                onDismiss = { locationError = null }
            )
        }
        Scaffold(bottomBar = {
            if (dataLoaded)
                BottomNavigationBar(navController) }) { innerPadding ->
            NavHost(navController,
                startDestination = Screen.Today.route,
                modifier = Modifier.padding(innerPadding)) {
                composable(Screen.Today.route) {
                    WeatherScreen(weatherViewModel, onLoaded = { dataLoaded = true })
                }
                composable(Screen.Daily.route) {
                    DailyForecastScreen(weatherViewModel, onLoaded = { dataLoaded = true })
                }
                composable(Screen.ZekAI.route) {
                    ZekAIScreen(weatherViewModel)
                }
                composable(Screen.SelectCity.route) {
                    CitySearchScreen(weatherViewModel) { city ->
                        val lat = city.lat
                        val lon = city.lon
                        weatherViewModel.saveLocation(lat, lon)
                        weatherViewModel.fetchWeather(lat, lon)
                        navController.navigate(Screen.Today.route)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.permission_rationale_title))
        },
        text = {
            Text(text = stringResource(id = R.string.permission_rationale_message))
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text(stringResource(id = R.string.grant_permission))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.error_title))
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.ok))
            }
        }
    )
}

suspend fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onNewLocation: (Double, Double) -> Unit
): Result<Unit> = runCatching {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    ) {
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        location?.let {
            onNewLocation(it.latitude, it.longitude)
        } ?: throw Exception("Konum alınamadı")
    } else {
        throw Exception("İzin verilmedi")
    }
}
