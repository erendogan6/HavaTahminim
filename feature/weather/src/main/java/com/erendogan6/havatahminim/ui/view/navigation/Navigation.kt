package com.erendogan6.havatahminim.ui.view.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.erendogan6.havatahminim.ui.component.WeatherText
import com.erendogan6.havatahminim.ui.theme.WeatherTheme

/** Bottom destinations bar for portrait (regular-height) windows. */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar(modifier = modifier, containerColor = WeatherTheme.colors.cardSurfaceFaint) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                // The label already announces the destination to TalkBack.
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { WeatherText(stringResource(id = destination.labelRes)) },
                selected = currentDestination.isOn(destination),
                onClick = { navController.navigateToTopLevel(destination) },
            )
        }
    }
}

/** Side rail for compact-height (landscape) windows. Insets are zeroed; the host applies them. */
@Composable
fun WeatherNavigationRail(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationRail(
        modifier = modifier,
        containerColor = WeatherTheme.colors.cardSurfaceFaint,
        windowInsets = WindowInsets(0.dp),
    ) {
        CenteredRailContent {
            TopLevelDestination.entries.forEach { destination ->
                NavigationRailItem(
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { WeatherText(stringResource(id = destination.labelRes)) },
                    selected = currentDestination.isOn(destination),
                    onClick = { navController.navigateToTopLevel(destination) },
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

/** True when [destination]'s route is anywhere in the current back-stack hierarchy. */
private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

/** Standard bottom-nav behaviour: single copy per tab, state saved and restored across switches. */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}