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
 * The feature's navigation graph and single entry point; :app hosts it without depending on the
 * individual screens.
 *
 * @param onUseMyLocation lives in the host because it drives the permission launcher.
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
