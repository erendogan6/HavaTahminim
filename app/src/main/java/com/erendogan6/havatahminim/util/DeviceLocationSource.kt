package com.erendogan6.havatahminim.util

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** A single device-position fix. */
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
)

/**
 * The seam over the platform's positioning stack: keeps `Context` and Play services out of
 * [com.erendogan6.havatahminim.ui.viewModel.MainViewModel], so the start-up orchestration
 * (GPS vs saved vs default) stays JVM-testable.
 */
interface DeviceLocationSource {
    /**
     * One high-accuracy fix, or null when it cannot be produced (permission missing, location
     * providers disabled, or the provider fails). Callers own the fallback decision, not this
     * layer.
     */
    suspend fun currentLocation(): DeviceLocation?
}

/** Fused-provider implementation — pure platform glue, exercised on device rather than in unit tests. */
@Singleton
class FusedDeviceLocationSource
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : DeviceLocationSource {
        private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(context) }

        override suspend fun currentLocation(): DeviceLocation? {
            if (!hasLocationPermission() || !isLocationServiceEnabled()) return null
            return try {
                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .await()
                    ?.let { DeviceLocation(it.latitude, it.longitude) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }

        private fun hasLocationPermission(): Boolean =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PermissionChecker.PERMISSION_GRANTED

        private fun isLocationServiceEnabled(): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
