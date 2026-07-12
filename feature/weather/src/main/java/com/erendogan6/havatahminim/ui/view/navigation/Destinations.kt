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
 * Type-safe navigation routes. Each is a @Serializable key the NavHost matches on, so
 * `navigate(TodayRoute)` and `composable<TodayRoute>` are checked by the compiler instead of
 * relying on magic strings. The four tabs take no arguments; presentation metadata (icon + label)
 * is deliberately kept out of the route types and lives in [TopLevelDestination], so the routes
 * stay pure navigation keys. They're internal because the feature exposes composable entry points
 * ([WeatherNavHost], the nav bars), not its route vocabulary.
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
