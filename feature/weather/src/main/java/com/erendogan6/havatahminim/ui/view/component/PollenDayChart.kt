package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.extension.toHourMinute
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenSeries
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.ui.component.WeatherText
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import com.erendogan6.havatahminim.util.PollenLevel

/** Hourly line chart of pollen concentration across the day (one colored line per pollen type). */
@Composable
internal fun PollenDayChart(
    hours: List<Long>,
    series: List<PollenSeries>,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val colors = WeatherTheme.colors

    // Global peak across the shown species: what peaks, when and how much.
    var peakType: PollenType? = null
    var peakIndex = 0
    var peakValue = 0.0
    series.forEach { s ->
        s.values.forEachIndexed { i, v ->
            val value = v ?: 0.0
            if (value > peakValue) {
                peakValue = value
                peakType = s.type
                peakIndex = i
            }
        }
    }
    val axisMax = (peakValue * 1.15).takeIf { it > 0.0 } ?: 1.0 // headroom so the peak isn't clipped

    // A Canvas is invisible to TalkBack; describe the peak in words.
    val chartDescription =
        peakType?.let { pt ->
            stringResource(R.string.a11y_pollen_chart) + ". " +
                stringResource(R.string.pollen_peak) + ": " +
                stringResource(PollenLevel.typeNameRes(pt)) + ", " +
                hours.getOrElse(peakIndex) { 0L }.toHourMinute(locale) + ", " +
                peakValue.toInt() + " " + stringResource(R.string.pollen_unit)
        } ?: stringResource(R.string.a11y_pollen_chart)

    Column(modifier = modifier.fillMaxWidth()) {
        peakType?.let { pt ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.typeColor(pt)),
                )
                Spacer(Modifier.size(6.dp))
                WeatherText(
                    text =
                        "${stringResource(R.string.pollen_peak)}: " +
                            "${stringResource(PollenLevel.typeNameRes(pt))} · " +
                            "${hours.getOrElse(peakIndex) { 0L }.toHourMinute(locale)} · " +
                            "${peakValue.toInt()} ${stringResource(R.string.pollen_unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink,
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = chartDescription }
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.chartBackground)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
        ) {
            val w = size.width
            val h = size.height
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { f ->
                drawLine(
                    color = colors.chartGrid,
                    start = Offset(0f, h * f),
                    end = Offset(w, h * f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            val lineStyle = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            series.forEach { s ->
                val n = s.values.size
                if (n >= 2) {
                    val path = Path()
                    s.values.forEachIndexed { i, v ->
                        val x = i / (n - 1f) * w
                        val y = h - ((v ?: 0.0) / axisMax).toFloat() * h
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = colors.typeColor(s.type), style = lineStyle)
                }
            }
            // Highlight the peak point.
            peakType?.let { pt ->
                val n = series.first { it.type == pt }.values.size
                if (n >= 2) {
                    val x = peakIndex / (n - 1f) * w
                    val y = h - (peakValue / axisMax).toFloat() * h
                    drawCircle(colors.chartBackground, radius = 5.dp.toPx(), center = Offset(x, y))
                    drawCircle(colors.typeColor(pt), radius = 4.dp.toPx(), center = Offset(x, y))
                }
            }
        }
        Spacer(Modifier.size(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val n = hours.size
            if (n > 0) {
                listOf(0, n / 4, n / 2, 3 * n / 4, n - 1).distinct().forEach { idx ->
                    WeatherText(
                        text = hours[idx].toHourMinute(locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.ink,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChartLegend(
    readings: List<PollenReading>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        readings.forEach { reading ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(WeatherTheme.colors.typeColor(reading.type)),
                )
                Spacer(Modifier.size(6.dp))
                WeatherText(
                    text =
                        "${stringResource(PollenLevel.typeNameRes(reading.type))} · " +
                            stringResource(PollenLevel.riskLabelRes(reading.risk)),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
