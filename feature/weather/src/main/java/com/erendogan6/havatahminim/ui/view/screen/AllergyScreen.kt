package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.erendogan6.havatahminim.ui.component.WeatherCard
import com.erendogan6.havatahminim.ui.component.WeatherText
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.DailyPollenForecast
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenSeries
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.AqiLevel
import com.erendogan6.havatahminim.util.PollenLevel
import java.text.SimpleDateFormat

@Composable
fun AllergyScreen(weatherViewModel: WeatherViewModel) {
    val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
    val airQuality by weatherViewModel.airQuality.collectAsStateWithLifecycle()
    val selectedAllergens by weatherViewModel.allergenPrefs.collectAsStateWithLifecycle()

    WeatherBackgroundLayout(weatherState) {
        Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
            if (airQuality == null) {
                SplashScreen()
            } else {
                AllergyContent(
                    airQuality = airQuality!!,
                    selectedAllergens = selectedAllergens,
                    onToggleAllergen = { type, sensitive -> weatherViewModel.toggleAllergen(type, sensitive) },
                )
            }
        }
    }
}

/** Allergens to summarize: the user's selection, or all six when nothing is selected. */
private fun List<PollenReading>.relevant(selected: Set<PollenType>) =
    filter { selected.isEmpty() || it.type in selected }

/** Most concerning reading: highest risk bucket first, then highest actual concentration. */
private fun worst(readings: List<PollenReading>): PollenReading? =
    readings.maxWithOrNull(compareBy({ it.risk.ordinal }, { it.valueGrains ?: 0.0 }))

@Composable
private fun AllergyContent(
    airQuality: AirQualityInfo,
    selectedAllergens: Set<PollenType>,
    onToggleAllergen: (PollenType, Boolean) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.size(16.dp))

        if (airQuality.pollenAvailable) {
            HeroCard(worst(airQuality.pollen.relevant(selectedAllergens)))
        } else {
            InfoCard(stringResource(R.string.pollen_unavailable))
        }

        Spacer(Modifier.size(16.dp))
        AllergenSelector(selectedAllergens, onToggleAllergen)

        if (airQuality.pollenAvailable) {
            Spacer(Modifier.size(20.dp))
            SectionTitle(stringResource(R.string.pollen_section_title))
            Spacer(Modifier.size(8.dp))
            airQuality.pollen.relevant(selectedAllergens).forEach { reading ->
                PollenBarRow(reading, highlighted = reading.type in selectedAllergens)
            }

            if (airQuality.dailyForecast.isNotEmpty()) {
                Spacer(Modifier.size(20.dp))
                SectionTitle(stringResource(R.string.pollen_forecast_title))
                Spacer(Modifier.size(8.dp))
                DailyForecast(airQuality.dailyForecast, selectedAllergens)
            }
        }

        Spacer(Modifier.size(20.dp))
        SectionTitle(stringResource(R.string.air_quality_section_title))
        Spacer(Modifier.size(8.dp))
        AirQualityCard(airQuality)
        Spacer(Modifier.size(16.dp))
    }
}

@Composable
private fun HeroCard(worst: PollenReading?) {
    val risk = worst?.risk ?: PollenRisk.NONE
    val onColored = WeatherTheme.colors.onColoredCard
    WeatherCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = WeatherTheme.colors.riskColor(risk),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            WeatherText(
                text = stringResource(R.string.allergy_today_risk),
                color = onColored,
                style = MaterialTheme.typography.bodyLarge,
            )
            WeatherText(
                text = stringResource(PollenLevel.riskLabelRes(risk)).uppercase(),
                color = onColored,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        shadow = Shadow(color = WeatherTheme.colors.shadowTinted, blurRadius = 3f),
                    ),
            )
            if (worst != null && risk != PollenRisk.NONE) {
                WeatherText(
                    text =
                        "${stringResource(R.string.allergy_top)}: " +
                            stringResource(PollenLevel.typeNameRes(worst.type)),
                    color = onColored,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllergenSelector(
    selected: Set<PollenType>,
    onToggle: (PollenType, Boolean) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(WeatherTheme.colors.surfaceVeil)
                .padding(12.dp),
    ) {
        WeatherText(
            text = stringResource(R.string.allergy_settings_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.size(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PollenType.entries.forEach { type ->
                val isSelected = type in selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggle(type, !isSelected) },
                    label = { WeatherText(stringResource(PollenLevel.typeNameRes(type))) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WeatherTheme.colors.cardSurface),
                )
            }
        }
    }
}

@Composable
private fun PollenBarRow(
    reading: PollenReading,
    highlighted: Boolean,
) {
    val container = if (highlighted) WeatherTheme.colors.cardSurfaceHighlighted else WeatherTheme.colors.cardSurface
    WeatherCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        containerColor = container,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                WeatherText(
                    text = stringResource(PollenLevel.typeNameRes(reading.type)),
                    style = MaterialTheme.typography.titleSmall,
                )
                WeatherText(
                    text = "${(reading.valueGrains ?: 0.0).toInt()} ${stringResource(R.string.pollen_unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WeatherTheme.colors.mutedText,
                )
            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    LevelBar(
                        fraction = PollenLevel.fraction(reading.type, reading.valueGrains),
                        color = WeatherTheme.colors.riskColor(reading.risk),
                    )
                }
                Spacer(Modifier.size(10.dp))
                WeatherText(
                    text = stringResource(PollenLevel.riskLabelRes(reading.risk)),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = WeatherTheme.colors.riskColor(reading.risk),
                )
            }
        }
    }
}

@Composable
private fun LevelBar(
    fraction: Float,
    color: Color,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(WeatherTheme.colors.barTrack),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color),
        )
    }
}

@Composable
private fun DailyForecast(
    days: List<DailyPollenForecast>,
    selected: Set<PollenType>,
) {
    // Accordion: one day expanded at a time; today (index 0) open by default.
    var expandedIndex by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxWidth()) {
        days.forEachIndexed { index, day ->
            DailyDayCard(
                day = day,
                selected = selected,
                expanded = expandedIndex == index,
                onToggle = { expandedIndex = if (expandedIndex == index) -1 else index },
            )
        }
    }
}

@Composable
private fun DailyDayCard(
    day: DailyPollenForecast,
    selected: Set<PollenType>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val dayName = SimpleDateFormat("EEEE", locale).format(day.date * 1000L)
    val relevant = day.readings.relevant(selected)
    val dayWorst = worst(relevant)
    val worstRisk = dayWorst?.risk ?: PollenRisk.NONE

    WeatherCard(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Column(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                WeatherText(
                    text = dayName.replaceFirstChar { it.uppercase(locale) },
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskBadge(
                        text = stringResource(PollenLevel.riskLabelRes(worstRisk)),
                        color = WeatherTheme.colors.riskColor(worstRisk),
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                val active = relevant.filter { it.risk != PollenRisk.NONE }
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    if (active.isEmpty()) {
                        WeatherText(
                            text = stringResource(R.string.pollen_all_low),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WeatherTheme.colors.mutedText,
                        )
                    } else {
                        val activeTypes = active.map { it.type }.toSet()
                        PollenDayChart(day.hours, day.hourly.filter { it.type in activeTypes })
                        Spacer(Modifier.size(10.dp))
                        ChartLegend(active)
                    }
                }
            }
        }
    }
}

/** Hourly line chart of pollen concentration across the day (one colored line per pollen type). */
@Composable
private fun PollenDayChart(
    hours: List<Long>,
    series: List<PollenSeries>,
) {
    val locale = LocalConfiguration.current.locales[0]
    val timeFmt = remember(locale) { SimpleDateFormat("HH:mm", locale) }
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

    Column(modifier = Modifier.fillMaxWidth()) {
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
                            "${timeFmt.format(hours.getOrElse(peakIndex) { 0L } * 1000L)} · " +
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
                        text = timeFmt.format(hours[idx] * 1000L),
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
private fun ChartLegend(readings: List<PollenReading>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun AirQualityCard(info: AirQualityInfo) {
    WeatherCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                WeatherText(
                    text = stringResource(R.string.european_aqi),
                    style = MaterialTheme.typography.titleSmall,
                )
                RiskBadge(
                    text = "${info.europeanAqi ?: "-"} · ${stringResource(AqiLevel.labelRes(info.europeanAqi))}",
                    color = WeatherTheme.colors.aqiColor(info.europeanAqi),
                )
            }
            Spacer(Modifier.size(10.dp))
            MetricRow(stringResource(R.string.pm2_5), info.pm25)
            MetricRow(stringResource(R.string.pm10), info.pm10)
            MetricRow(stringResource(R.string.ozone), info.ozone)
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: Double?,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    ) {
        WeatherText(label, style = MaterialTheme.typography.bodyLarge)
        WeatherText(
            text = value?.let { "${it.toInt()} ${stringResource(R.string.unit_ugm3)}" } ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RiskBadge(
    text: String,
    color: Color,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(color)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        WeatherText(
            text = text,
            color = WeatherTheme.colors.onColoredCard,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    WeatherText(
        text = text,
        style =
            MaterialTheme.typography.titleLarge.copy(
                shadow = Shadow(color = WeatherTheme.colors.shadowSoft, blurRadius = 1f),
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InfoCard(message: String) {
    WeatherCard(modifier = Modifier.fillMaxWidth()) {
        WeatherText(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}
