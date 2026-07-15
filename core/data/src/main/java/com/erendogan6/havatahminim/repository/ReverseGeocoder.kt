package com.erendogan6.havatahminim.repository

import android.content.Context
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The seam over the platform [Geocoder]: keeps `Context` (and the deprecated blocking API) out of
 * [LocationRepository], so location logic stays JVM-testable.
 */
interface ReverseGeocoder {
    /** Best human-readable name for the coordinates, or "" when geocoding is unavailable. */
    fun resolve(
        lat: Double,
        lon: Double,
        language: String,
    ): String
}

/**
 * Open-Meteo's forecast endpoint does not return a place name, so we reverse-geocode the
 * coordinates with the platform [Geocoder]. Falls back gracefully when geocoding is unavailable
 * or returns nothing. Pure platform glue, exercised on device rather than in unit tests.
 */
@Singleton
class AndroidReverseGeocoder
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ReverseGeocoder {
        @Suppress("DEPRECATION")
        override fun resolve(
            lat: Double,
            lon: Double,
            language: String,
        ): String =
            try {
                val geocoder = Geocoder(context, Locale(language))
                val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                    ?: address?.countryName
                    ?: ""
            } catch (e: Exception) {
                ""
            }
    }
