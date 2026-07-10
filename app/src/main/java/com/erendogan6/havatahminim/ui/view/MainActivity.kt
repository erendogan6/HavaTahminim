package com.erendogan6.havatahminim.ui.view

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.erendogan6.havatahminim.extension.NetworkUtils
import com.erendogan6.havatahminim.ui.adaptive.isCompactHeight
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.view.component.BackgroundImage
import com.erendogan6.havatahminim.ui.view.navigation.BottomNavigationBar
import com.erendogan6.havatahminim.ui.view.navigation.Screen
import com.erendogan6.havatahminim.ui.view.navigation.WeatherNavigationRail
import com.erendogan6.havatahminim.ui.view.screen.AllergyScreen
import com.erendogan6.havatahminim.ui.view.screen.DailyForecastScreen
import com.erendogan6.havatahminim.ui.view.screen.WeatherScreen
import com.erendogan6.havatahminim.ui.view.screen.ZekAIScreen
import com.erendogan6.havatahminim.ui.viewModel.MainViewModel
import com.erendogan6.havatahminim.util.NotificationUtils
import com.erendogan6.havatahminim.util.getCurrentLocation
import com.erendogan6.havatahminim.util.isLocationServiceEnabled
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
        )
        setContent {
            HavaTahminimApp()
        }
    }
}

@Composable
fun HavaTahminimApp() {
    HavaTahminimTheme {
        val mainViewModel: MainViewModel = hiltViewModel()
        val context = LocalContext.current
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val coroutineScope = rememberCoroutineScope()

        var locationPermissionGranted by rememberSaveable { mutableStateOf(false) }
        var showPermissionRationale by rememberSaveable { mutableStateOf(false) }
        var notificationPermissionGranted by rememberSaveable { mutableStateOf(false) }
        var locationError by rememberSaveable { mutableStateOf<String?>(null) }
        val showNoInternetDialog = rememberSaveable { mutableStateOf(false) }
        val navController = rememberNavController()

        // Activity chrome keys off the shared current weather: the full-bleed background swaps
        // from the splash photo, and the nav bar/rail appears, once the first weather arrives.
        val weatherState by mainViewModel.currentWeather.collectAsStateWithLifecycle()
        val weatherReady = weatherState != null

        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                notificationPermissionGranted = isGranted
                if (isGranted) {
                    NotificationUtils.scheduleDailyNotification(context)
                } else {
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
                    coroutineScope.launch {
                        if (isLocationServiceEnabled(context) && NetworkUtils.isNetworkAvailable(context)) {
                            resolveGpsLocation(context, fusedLocationClient, mainViewModel, showNoInternetDialog)
                        } else {
                            startFromSavedOrDefault(mainViewModel, context, showNoInternetDialog)
                        }
                    }
                } else {
                    startFromSavedOrDefault(mainViewModel, context, showNoInternetDialog)
                }
            }

        // Triggered by the "my location" icon on the Today screen: re-resolve the GPS position and
        // point the whole app at it. Requests permission first if it isn't granted yet.
        val onUseMyLocation: () -> Unit = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                coroutineScope.launch {
                    if (isLocationServiceEnabled(context) && NetworkUtils.isNetworkAvailable(context)) {
                        getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                            mainViewModel.setLocation(lat, lon)
                        }
                    } else {
                        showNoInternetDialog.value = true
                    }
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        LaunchedEffect(Unit) {
            if (!weatherReady) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    requestNotificationPermission(notificationPermissionLauncher)
                    coroutineScope.launch {
                        resolveGpsLocation(context, fusedLocationClient, mainViewModel, showNoInternetDialog)
                    }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
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

        locationError?.let {
            ErrorDialog(
                message = it,
                onDismiss = { locationError = null },
            )
        }

        if (showNoInternetDialog.value) {
            NoInternetDialog { showNoInternetDialog.value = false }
        }

        // Compact height (landscape phone): navigation moves from the bottom bar to a side
        // rail so the content keeps its vertical space.
        val compactHeight = isCompactHeight()

        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed background drawn behind the transparent system bars. The per-screen
            // content (below) stays inset via the Scaffold's innerPadding.
            BackgroundImage(weatherState)

            Scaffold(
                containerColor = Color.Transparent,
                // safeDrawing (systemBars + display cutout) keeps content clear of the side
                // nav bar and camera cutout in landscape.
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (weatherReady && !compactHeight) {
                        BottomNavigationBar(navController)
                    }
                },
            ) { innerPadding ->
                Row(modifier = Modifier.padding(innerPadding)) {
                    if (weatherReady && compactHeight) {
                        WeatherNavigationRail(navController)
                    }
                    NavHost(
                        navController,
                        startDestination = Screen.Today.route,
                        modifier = Modifier.weight(1f),
                    ) {
                        composable(Screen.Today.route) {
                            WeatherScreen(onUseMyLocation = onUseMyLocation)
                        }
                        composable(Screen.Daily.route) {
                            DailyForecastScreen()
                        }
                        composable(Screen.Allergy.route) {
                            AllergyScreen()
                        }
                        composable(Screen.ZekAI.route) {
                            ZekAIScreen()
                        }
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

/** Resolves a GPS fix into the app-wide active location; falls back to saved/default on failure. */
private suspend fun resolveGpsLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    mainViewModel: MainViewModel,
    showNoInternetDialog: MutableState<Boolean>,
) {
    val locationResult =
        getCurrentLocation(context, fusedLocationClient) { lat, lon ->
            if (NetworkUtils.isNetworkAvailable(context)) {
                mainViewModel.setLocation(lat, lon)
            } else {
                showNoInternetDialog.value = true
            }
        }

    if (locationResult.isFailure) {
        startFromSavedOrDefault(mainViewModel, context, showNoInternetDialog)
    }
}

private fun startFromSavedOrDefault(
    mainViewModel: MainViewModel,
    context: Context,
    showNoInternetDialog: MutableState<Boolean>,
) {
    if (!NetworkUtils.isNetworkAvailable(context)) {
        showNoInternetDialog.value = true
    }
    mainViewModel.startFromSavedOrDefault()
}
