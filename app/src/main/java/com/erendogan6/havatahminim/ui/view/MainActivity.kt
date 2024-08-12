package com.erendogan6.havatahminim.ui.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.navigation.NavController
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
import com.erendogan6.havatahminim.util.NotificationReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HavaTahminimApp()
        }
        scheduleDailyNotification(this)
    }
}

fun scheduleDailyNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    val calendar =
        Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent,
    )
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
        var notificationPermissionGranted by remember { mutableStateOf(false) }
        var locationError by remember { mutableStateOf<String?>(null) }
        val showNoInternetDialog = remember { mutableStateOf(false) }  // MutableState<Boolean> olarak tanımlandı
        val navController = rememberNavController()
        var dataLoaded by remember { mutableStateOf(false) }
        val savedLocation by weatherViewModel.location.collectAsState()

        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                notificationPermissionGranted = isGranted
                if (!isGranted) {
                    showPermissionRationale = true
                }
            }

        val locationPermissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                locationPermissionGranted = isGranted
                if (isGranted) {
                    requestNotificationPermission(notificationPermissionLauncher)
                } else {
                    useDefaultLocation(weatherViewModel, context, navController, showNoInternetDialog)
                }
            }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
                requestNotificationPermission(notificationPermissionLauncher)
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        if (showPermissionRationale && (!locationPermissionGranted || !notificationPermissionGranted)) {
            PermissionRationaleDialog(
                onDismiss = { showPermissionRationale = false },
                onRequestPermission = {
                    showPermissionRationale = false
                    if (!locationPermissionGranted) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else if (!notificationPermissionGranted) {
                        requestNotificationPermission(notificationPermissionLauncher)
                    }
                },
            )
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
                        useDefaultLocation(weatherViewModel, context, navController, showNoInternetDialog)
                    }
                }
            }
        }

        locationError?.let {
            ErrorDialog(
                message = it,
                onDismiss = { locationError = null },
            )
        }

        if (showNoInternetDialog.value) {
            NoInternetDialog { showNoInternetDialog.value = false }
        }

        Scaffold(bottomBar = {
            if (dataLoaded) {
                BottomNavigationBar(navController)
            }
        }) { innerPadding ->
            NavHost(
                navController,
                startDestination = Screen.Today.route,
                modifier = Modifier.padding(innerPadding),
            ) {
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

private fun requestNotificationPermission(notificationPermissionLauncher: ActivityResultLauncher<String>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun useDefaultLocation(
    weatherViewModel: WeatherViewModel,
    context: Context,
    navController: NavController,
    showNoInternetDialog: MutableState<Boolean>
) {
    // İstanbul koordinatları
    val defaultLat = 41.0082
    val defaultLon = 28.9784
    if (NetworkUtils.isNetworkAvailable(context)) {
        weatherViewModel.fetchWeather(defaultLat, defaultLon)
        navController.navigate(Screen.Today.route)
    } else {
        showNoInternetDialog.value = true
    }
}

@Composable
fun NoInternetDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "İnternet Bağlantısı Yok")
        },
        text = {
            Text(text = "Hava durumu verilerini almak için internet bağlantısına ihtiyacınız var. Lütfen internet bağlantınızı kontrol edin ve tekrar deneyin.")
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tamam")
            }
        },
    )
}

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
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
        },
    )
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
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
        },
    )
}

suspend fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onNewLocation: (Double, Double) -> Unit,
): Result<Unit> =
    runCatching {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            val location =
                fusedLocationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null,
                    ).await()

            location?.let {
                onNewLocation(it.latitude, it.longitude)
            } ?: throw Exception("Konum alınamadı")
        } else {
            throw Exception("İzin verilmedi")
        }
    }

