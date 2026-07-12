package com.erendogan6.havatahminim.ui.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.erendogan6.havatahminim.ui.view.screen.AllergyScreen
import com.erendogan6.havatahminim.ui.view.screen.DailyForecastScreen
import com.erendogan6.havatahminim.ui.view.screen.WeatherScreen
import com.erendogan6.havatahminim.ui.view.screen.ZekAIScreen

/**
 * The weather feature's navigation graph and single entry point. The host module (`:app`) owns the
 * surrounding chrome — the full-bleed background and the adaptive bottom bar / side rail — but
 * delegates the destination graph here, so it never depends on the individual screens. The typed
 * `composable<Route>` builders match the [TodayRoute]/[DailyRoute]/… keys declared alongside.
 *
 * @param onUseMyLocation forwarded to the Today screen's "use my location" action; it lives in the
 *   host because it drives the runtime location-permission launcher.
 */
@Composable
fun WeatherNavHost(
    navController: NavHostController,
    onUseMyLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TodayRoute,
        modifier = modifier,
    ) {
        composable<TodayRoute> { WeatherScreen(onUseMyLocation = onUseMyLocation) }
        composable<DailyRoute> { DailyForecastScreen() }
        composable<AllergyRoute> { AllergyScreen() }
        composable<ZekAiRoute> { ZekAIScreen() }
    }
}
