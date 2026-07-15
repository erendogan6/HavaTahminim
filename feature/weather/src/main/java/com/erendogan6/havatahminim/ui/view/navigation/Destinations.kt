package com.erendogan6.havatahminim.ui.view.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.ui.graphics.vector.ImageVector
import com.erendogan6.havatahminim.feature.weather.R
import kotlinx.serialization.Serializable

/*
 * Type-safe navigation routes: @Serializable keys matched by `composable<TodayRoute>` and
 * `navigate(TodayRoute)`, so destinations are compiler-checked instead of string-matched.
 * The four tabs take no arguments. Presentation metadata (icon + label) lives in
 * [TopLevelDestination], keeping the routes pure navigation keys. Internal: the feature
 * exposes [WeatherNavHost] and the nav bars, not the routes themselves.
 */
@Serializable internal data object TodayRoute

@Serializable internal data object DailyRoute

@Serializable internal data object AllergyRoute

@Serializable internal data object ZekAiRoute

/** The four bottom-nav destinations, in bar order, each paired with its route, icon and label. */
internal enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
) {
    TODAY(TodayRoute, Icons.Default.Home, R.string.today),
    DAILY(DailyRoute, Icons.Default.DateRange, R.string.daily),
    ALLERGY(AllergyRoute, Icons.Default.LocalFlorist, R.string.allergy),
    ZEKAI(ZekAiRoute, Icons.Default.Face, R.string.zekai),
}
