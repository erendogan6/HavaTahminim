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
 * Type-safe navigation routes, matched by composable<TodayRoute> / navigate(TodayRoute).
 * Icon and label metadata lives in [TopLevelDestination] so the routes stay pure keys.
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
