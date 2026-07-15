package com.erendogan6.havatahminim.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw palette, internal to the design system. Consumers go through
 * [androidx.compose.material3.MaterialTheme.colorScheme] or [WeatherColors].
 */
internal object Palette {
    // Brand blues
    val SkyMist = Color(0xFFB6DDF1) // static (< Android 12) scheme primary
    val SkyCard = Color(0xAA80C4E9) // translucent sky-blue card surface
    val SkyCardHighlighted = Color(0xCC80C4E9)
    val CityBlue = Color(0xFFBBDEFB)
    val PrecipitationBlue = Color(0xFF1565C0)
    val Ink = Color(0xFF1B3A4B) // deep navy text/icon on the photo background

    // Neutrals / effects
    val White = Color.White
    val Black = Color.Black
    val MutedText = Color.DarkGray
    val ShadowDark = Color.DarkGray
    val ShadowSoft = Color.Gray
    val ShadowTinted = Color(0x55000000)
    val SurfaceVeil = Color(0x66FFFFFF)
    val ChartBackground = Color(0xFFFFFFFF)
    val ChartGrid = Color(0x11000000)
    val BarTrack = Color(0x33000000)

    // Pollen / air-quality risk scale (shared by risk badges and AQI bands)
    val RiskNone = Color(0xFF9E9E9E)
    val RiskLow = Color(0xFF4CAF50)
    val RiskModerate = Color(0xFFFFC107)
    val RiskHigh = Color(0xFFFF9800)
    val RiskVeryHigh = Color(0xFFF44336)
    val AqiFair = Color(0xFF8BC34A)
    val AqiExtremelyPoor = Color(0xFF9C27B0)

    // Pollen species line colors for the intra-day chart
    val SpeciesAlder = Color(0xFF00897B) // teal
    val SpeciesBirch = Color(0xFF6D4C41) // brown
    val SpeciesGrass = Color(0xFF2E7D32) // green
    val SpeciesMugwort = Color(0xFF8E24AA) // purple
    val SpeciesOlive = Color(0xFF9E9D24) // olive
    val SpeciesRagweed = Color(0xFFE53935) // red
}
