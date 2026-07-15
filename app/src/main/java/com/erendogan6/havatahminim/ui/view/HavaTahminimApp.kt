package com.erendogan6.havatahminim.ui.view

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.erendogan6.havatahminim.ui.adaptive.isCompactHeight
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.view.component.BackgroundImage
import com.erendogan6.havatahminim.ui.view.navigation.BottomNavigationBar
import com.erendogan6.havatahminim.ui.view.navigation.WeatherNavHost
import com.erendogan6.havatahminim.ui.view.navigation.WeatherNavigationRail
import com.erendogan6.havatahminim.ui.viewModel.MainViewModel
import com.erendogan6.havatahminim.util.NotificationUtils

/**
 * Root composable: background, navigation chrome and the runtime-permission flow. The launchers
 * must stay in the composition (they bind to the activity result registry); their callbacks just
 * report the outcome to [MainViewModel].
 */
@Composable
fun HavaTahminimApp() {
    HavaTahminimTheme {
        val mainViewModel: MainViewModel = hiltViewModel()
        val context = LocalContext.current
        val navController = rememberNavController()

        // The photo background and the nav bar appear once the first weather arrives.
        val weatherState by mainViewModel.currentWeather.collectAsStateWithLifecycle()
        val weatherReady = weatherState != null
        val showNoInternetDialog by mainViewModel.showNoInternetDialog.collectAsStateWithLifecycle()

        var locationPermissionGranted by rememberSaveable { mutableStateOf(false) }
        var notificationPermissionGranted by rememberSaveable { mutableStateOf(false) }
        var showPermissionRationale by rememberSaveable { mutableStateOf(false) }

        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                notificationPermissionGranted = isGranted
                if (isGranted) {
                    NotificationUtils.scheduleDailyNotification(context)
                } else {
                    showPermissionRationale = true
                }
            }

        val locationPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                locationPermissionGranted = isGranted
                if (isGranted) {
                    requestNotificationPermission(notificationPermissionLauncher)
                    mainViewModel.onLocationPermissionGranted()
                } else {
                    mainViewModel.onLocationPermissionDenied()
                }
            }

        // If permission is missing, the grant callback above resumes the flow.
        val onUseMyLocation: () -> Unit = {
            if (hasLocationPermission(context)) {
                mainViewModel.useCurrentLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        LaunchedEffect(Unit) {
            if (!weatherReady) {
                if (hasLocationPermission(context)) {
                    locationPermissionGranted = true
                    requestNotificationPermission(notificationPermissionLauncher)
                    mainViewModel.onLocationPermissionGranted()
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

        if (showNoInternetDialog) {
            NoInternetDialog { mainViewModel.dismissNoInternetDialog() }
        }

        // Landscape phones get a side rail instead of the bottom bar.
        val compactHeight = isCompactHeight()

        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed photo behind the transparent system bars; content stays inset.
            BackgroundImage(weatherState)

            Scaffold(
                containerColor = Color.Transparent,
                // Keeps content clear of the nav bar and camera cutout in landscape.
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
                    WeatherNavHost(
                        navController = navController,
                        onUseMyLocation = onUseMyLocation,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PermissionChecker.PERMISSION_GRANTED

private fun requestNotificationPermission(launcher: ActivityResultLauncher<String>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
