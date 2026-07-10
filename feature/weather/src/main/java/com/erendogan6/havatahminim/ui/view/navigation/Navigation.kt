package com.erendogan6.havatahminim.ui.view.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ColumnScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.ui.component.WeatherText
import com.erendogan6.havatahminim.ui.theme.WeatherTheme

sealed class Screen(
    val route: String,
    val icon: ImageVector,
    val title: Int,
) {
    data object Today : Screen("weather_screen", Icons.Default.Home, R.string.today)

    data object Daily : Screen("daily_forecast_screen", Icons.Default.DateRange, R.string.daily)

    data object Allergy : Screen("allergy_screen", Icons.Default.LocalFlorist, R.string.allergy)

    data object ZekAI : Screen("zekai", Icons.Default.Face, R.string.zekai)
}

private val screens = listOf(Screen.Today, Screen.Daily, Screen.Allergy, Screen.ZekAI)

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Bottom destinations bar for portrait (regular-height) windows. */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(modifier = Modifier.padding(0.dp), containerColor = WeatherTheme.colors.cardSurfaceFaint) {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.title)) },
                label = { WeatherText(stringResource(id = screen.title)) },
                selected = currentRoute == screen.route,
                onClick = { navController.navigateSingleTopTo(screen.route) },
            )
        }
    }
}

/**
 * Side rail for compact-height (landscape) windows, where a bottom bar would eat too much
 * vertical space. Insets are zeroed because the host applies them via the Scaffold padding.
 */
@Composable
fun WeatherNavigationRail(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationRail(
        containerColor = WeatherTheme.colors.cardSurfaceFaint,
        windowInsets = WindowInsets(0.dp),
    ) {
        CenteredRailContent {
            screens.forEach { screen ->
                NavigationRailItem(
                    icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.title)) },
                    label = { WeatherText(stringResource(id = screen.title)) },
                    selected = currentRoute == screen.route,
                    onClick = { navController.navigateSingleTopTo(screen.route) },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.CenteredRailContent(content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.weight(1f))
    content()
    Spacer(Modifier.weight(1f))
}
