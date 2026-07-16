package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenSeries
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.view.navigation.BottomNavigationBar
import com.erendogan6.havatahminim.ui.view.navigation.WeatherNavigationRail
import com.android.tools.screenshot.PreviewTest

/*
 * Golden corpus for the feature components: each @Preview here is rendered by
 * validateDebugScreenshotTest and compared against src/debugScreenshotTest/reference/.
 * Regenerate with updateDebugScreenshotTest after an intentional visual change.
 */

private const val SKY_BACKGROUND = 0xFF6FA8DC

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun ErrorMessagePreview() {
    HavaTahminimTheme(dynamicColor = false) {
        ErrorMessage(message = "İnternet bağlantısı yok. Lütfen bağlantınızı kontrol edip tekrar deneyin.")
    }
}

@PreviewTest
@Preview(heightDp = 320)
@Composable
fun BackgroundImageSplashPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        BackgroundImage(weatherState = null)
    }
}

@PreviewTest
@Preview
@Composable
fun BottomNavigationBarPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        BottomNavigationBar(navController = rememberNavController())
    }
}

@PreviewTest
@Preview(heightDp = 360)
@Composable
fun WeatherNavigationRailPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherNavigationRail(navController = rememberNavController())
    }
}
