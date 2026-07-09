package com.erendogan6.havatahminim.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * App-specific color tokens that have no Material3 [androidx.compose.material3.ColorScheme] slot:
 * the translucent cards drawn over the photo background, chart/bar scales, and the pollen/AQI
 * risk ramp. Read them via [WeatherTheme.colors]; never hardcode a [Color] in feature code.
 */
@Immutable
data class WeatherColors(
    // Surfaces over the photo background
    val cardSurface: Color,
    val cardSurfaceHighlighted: Color,
    val cardSurfaceFaint: Color,
    val surfaceVeil: Color,
    val citySurface: Color,
    // Content
    val ink: Color,
    val mutedText: Color,
    val onColoredCard: Color,
    val onCard: Color,
    val precipitation: Color,
    // Effects
    val glow: Color,
    val shadow: Color,
    val shadowSoft: Color,
    val shadowTinted: Color,
    // Charts
    val chartBackground: Color,
    val chartGrid: Color,
    val barTrack: Color,
    // Risk ramp (pollen levels + European AQI bands)
    val riskNone: Color,
    val riskLow: Color,
    val riskModerate: Color,
    val riskHigh: Color,
    val riskVeryHigh: Color,
    val aqiFair: Color,
    val aqiExtremelyPoor: Color,
    // Pollen species (chart lines / legend)
    val speciesAlder: Color,
    val speciesBirch: Color,
    val speciesGrass: Color,
    val speciesMugwort: Color,
    val speciesOlive: Color,
    val speciesRagweed: Color,
)

internal val LightWeatherColors =
    WeatherColors(
        cardSurface = Palette.SkyCard,
        cardSurfaceHighlighted = Palette.SkyCardHighlighted,
        cardSurfaceFaint = Palette.SkyCard.copy(alpha = 0.2f),
        surfaceVeil = Palette.SurfaceVeil,
        citySurface = Palette.CityBlue,
        ink = Palette.Ink,
        mutedText = Palette.MutedText,
        onColoredCard = Palette.White,
        onCard = Palette.Black,
        precipitation = Palette.PrecipitationBlue,
        glow = Palette.White,
        shadow = Palette.ShadowDark,
        shadowSoft = Palette.ShadowSoft,
        shadowTinted = Palette.ShadowTinted,
        chartBackground = Palette.ChartBackground,
        chartGrid = Palette.ChartGrid,
        barTrack = Palette.BarTrack,
        riskNone = Palette.RiskNone,
        riskLow = Palette.RiskLow,
        riskModerate = Palette.RiskModerate,
        riskHigh = Palette.RiskHigh,
        riskVeryHigh = Palette.RiskVeryHigh,
        aqiFair = Palette.AqiFair,
        aqiExtremelyPoor = Palette.AqiExtremelyPoor,
        speciesAlder = Palette.SpeciesAlder,
        speciesBirch = Palette.SpeciesBirch,
        speciesGrass = Palette.SpeciesGrass,
        speciesMugwort = Palette.SpeciesMugwort,
        speciesOlive = Palette.SpeciesOlive,
        speciesRagweed = Palette.SpeciesRagweed,
    )

internal val LocalWeatherColors =
    staticCompositionLocalOf<WeatherColors> {
        error("WeatherColors not provided — wrap the UI in HavaTahminimTheme")
    }
