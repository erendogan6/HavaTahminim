package com.erendogan6.havatahminim.ui.view.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.erendogan6.havatahminim.R

sealed class Screen(val route: String, val icon: ImageVector, val title: Int) {
    data object Today : Screen("weather_screen", Icons.Default.Home, R.string.today)
    data object Daily : Screen("daily_forecast_screen", Icons.Default.DateRange, R.string.daily)
    data object ZekAI : Screen("zekai", Icons.Default.Face, R.string.zekai)
    data object SelectCity : Screen("select_city", Icons.Default.LocationOn, R.string.select_city)
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(modifier = Modifier.padding(0.dp), containerColor = Color(0xFFFFF6E9)) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val screens = listOf(
            Screen.Today,
            Screen.Daily,
            Screen.ZekAI,
            Screen.SelectCity
        )
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.title)) },
                label = { Text(stringResource(id = screen.title)) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
