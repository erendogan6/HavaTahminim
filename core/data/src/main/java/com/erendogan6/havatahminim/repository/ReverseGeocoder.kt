package com.erendogan6.havatahminim.repository

import android.content.Context
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Reverse geocoding behind an interface, for testability. */
interface ReverseGeocoder {
    /** Best human-readable name for the coordinates, or "" when geocoding is unavailable. */
    fun resolve(
        lat: Double,
        lon: Double,
        language: String,
    ): String
}

/**
 * Platform [Geocoder] implementation; Open-Meteo returns no place name, so the coordinates are
 * reverse-geocoded here. Returns "" when geocoding is unavailable.
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
