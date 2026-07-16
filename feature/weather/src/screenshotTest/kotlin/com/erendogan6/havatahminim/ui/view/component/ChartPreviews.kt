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
fun PollenDayChartPreview() {
    val hours = List(8) { 1_752_555_600L + it * 3_600L }
    val series =
        listOf(
            PollenSeries(PollenType.GRASS, listOf(12.0, 18.0, 30.0, 55.0, 61.0, 42.0, 25.0, 14.0)),
            PollenSeries(PollenType.BIRCH, listOf(4.0, 6.0, 9.0, 12.0, 10.0, 8.0, 5.0, 3.0)),
        )
    HavaTahminimTheme(dynamicColor = false) {
        PollenDayChart(hours = hours, series = series)
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun ChartLegendPreview() {
    val readings =
        listOf(
            PollenReading(PollenType.GRASS, 61.0, PollenRisk.HIGH),
            PollenReading(PollenType.BIRCH, 12.0, PollenRisk.MODERATE),
        )
    HavaTahminimTheme(dynamicColor = false) {
        ChartLegend(readings = readings)
    }
}
