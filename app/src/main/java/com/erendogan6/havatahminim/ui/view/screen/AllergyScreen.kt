package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.DailyPollenForecast
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel
import com.erendogan6.havatahminim.util.AqiLevel
import com.erendogan6.havatahminim.util.PollenLevel
import java.text.SimpleDateFormat

private val OPEN_SANS = FontFamily(Font(R.font.open_sans))
private val CARD_BLUE = Color(0xAA80C4E9)

@Composable
fun AllergyScreen(weatherViewModel: WeatherViewModel) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val airQuality by weatherViewModel.airQuality.collectAsState()
    val selectedAllergens by weatherViewModel.allergenPrefs.collectAsState()

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

private fun worst(readings: List<PollenReading>): PollenReading? = readings.maxByOrNull { it.risk.ordinal }

private fun fractionFor(risk: PollenRisk): Float =
    when (risk) {
        PollenRisk.NONE -> 0.05f
        PollenRisk.LOW -> 0.28f
        PollenRisk.MODERATE -> 0.52f
        PollenRisk.HIGH -> 0.76f
        PollenRisk.VERY_HIGH -> 1f
    }

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
            airQuality.pollen.forEach { reading ->
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
    Card(
        colors = CardDefaults.cardColors(containerColor = PollenLevel.riskColor(risk)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = stringResource(R.string.allergy_today_risk),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = OPEN_SANS,
            )
            Text(
                text = stringResource(PollenLevel.riskLabelRes(risk)).uppercase(),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = OPEN_SANS,
                style = TextStyle(shadow = Shadow(color = Color(0x55000000), blurRadius = 3f)),
            )
            if (worst != null && risk != PollenRisk.NONE) {
                Text(
                    text =
                        "${stringResource(R.string.allergy_top)}: " +
                            stringResource(PollenLevel.typeNameRes(worst.type)),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = OPEN_SANS,
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
                .background(Color(0x66FFFFFF))
                .padding(12.dp),
    ) {
        Text(
            text = stringResource(R.string.allergy_settings_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = OPEN_SANS,
        )
        Spacer(Modifier.size(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PollenType.entries.forEach { type ->
                val isSelected = type in selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggle(type, !isSelected) },
                    label = { Text(stringResource(PollenLevel.typeNameRes(type))) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CARD_BLUE),
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
    Card(
        colors = CardDefaults.cardColors(containerColor = if (highlighted) Color(0xCC80C4E9) else CARD_BLUE),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(PollenLevel.typeNameRes(reading.type)),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = OPEN_SANS,
                )
                Text(
                    text = "${(reading.valueGrains ?: 0.0).toInt()} ${stringResource(R.string.pollen_unit)}",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    fontFamily = OPEN_SANS,
                )
            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) { LevelBar(reading.risk) }
                Spacer(Modifier.size(10.dp))
                Text(
                    text = stringResource(PollenLevel.riskLabelRes(reading.risk)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PollenLevel.riskColor(reading.risk),
                    fontFamily = OPEN_SANS,
                )
            }
        }
    }
}

@Composable
private fun LevelBar(risk: PollenRisk) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x33000000)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fractionFor(risk))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(PollenLevel.riskColor(risk)),
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

    Card(
        colors = CardDefaults.cardColors(containerColor = CARD_BLUE),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = dayName.replaceFirstChar { it.uppercase(locale) },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = OPEN_SANS,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskBadge(
                        text = stringResource(PollenLevel.riskLabelRes(worstRisk)),
                        color = PollenLevel.riskColor(worstRisk),
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    relevant.forEach { reading -> PollenMiniRow(reading) }
                }
            }
        }
    }
}

@Composable
private fun PollenMiniRow(reading: PollenReading) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(PollenLevel.typeNameRes(reading.type)),
                fontSize = 15.sp,
                fontFamily = OPEN_SANS,
            )
            Text(
                text = "${(reading.valueGrains ?: 0.0).toInt()} ${stringResource(R.string.pollen_unit)}",
                fontSize = 12.sp,
                color = Color.DarkGray,
                fontFamily = OPEN_SANS,
            )
        }
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { LevelBar(reading.risk) }
            Spacer(Modifier.size(10.dp))
            Text(
                text = stringResource(PollenLevel.riskLabelRes(reading.risk)),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PollenLevel.riskColor(reading.risk),
                fontFamily = OPEN_SANS,
            )
        }
    }
}

@Composable
private fun AirQualityCard(info: AirQualityInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CARD_BLUE),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.european_aqi),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = OPEN_SANS,
                )
                RiskBadge(
                    text = "${info.europeanAqi ?: "-"} · ${stringResource(AqiLevel.labelRes(info.europeanAqi))}",
                    color = AqiLevel.color(info.europeanAqi),
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
        Text(label, fontSize = 16.sp, fontFamily = OPEN_SANS)
        Text(
            text = value?.let { "${it.toInt()} ${stringResource(R.string.unit_ugm3)}" } ?: "-",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = OPEN_SANS,
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
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = OPEN_SANS,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = OPEN_SANS,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(shadow = Shadow(color = Color.Gray, blurRadius = 1f)),
    )
}

@Composable
private fun InfoCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CARD_BLUE),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            fontFamily = OPEN_SANS,
            modifier = Modifier.padding(16.dp),
        )
    }
}
