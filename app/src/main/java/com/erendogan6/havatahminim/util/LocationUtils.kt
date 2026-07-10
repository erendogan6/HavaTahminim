package com.erendogan6.havatahminim.util

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

internal fun isLocationServiceEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

/**
 * Resolves the device's current position and hands it to [onNewLocation]. The failure message
 * is developer-facing only (callers branch on `isFailure` and fall back to the last/default
 * location — it is never shown to the user).
 */
internal suspend fun getCurrentLocation(
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
            } ?: throw Exception("Location unavailable")
        } else {
            throw Exception("Location permission not granted")
        }
    }
