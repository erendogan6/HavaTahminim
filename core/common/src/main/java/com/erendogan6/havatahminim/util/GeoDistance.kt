package com.erendogan6.havatahminim.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Great-circle distance between two coordinates in meters (haversine).
 *
 * Replaces `android.location.Location.distanceTo` so distance logic is pure JVM and unit-testable
 * (the Android implementation is unavailable off-device, and `returnDefaultValues` would silently
 * return 0). Haversine assumes a spherical Earth; the drift vs the WGS84 ellipsoid is <0.5%,
 * immaterial against the app's 5 km / 10 km cache thresholds.
 */
fun distanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}
