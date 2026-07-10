package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.ui.component.WeatherCard
import com.erendogan6.havatahminim.ui.component.WeatherText
import com.erendogan6.havatahminim.ui.component.WeatherCard
import com.erendogan6.havatahminim.ui.component.WeatherTextField
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import com.erendogan6.havatahminim.ui.viewModel.CitySearchViewModel

@Composable
fun CitySearchScreen(
    modifier: Modifier = Modifier,
    viewModel: CitySearchViewModel = hiltViewModel(),
    onCitySelected: (City) -> Unit = {},
) {
    val cityState by viewModel.cities.collectAsStateWithLifecycle()
    val searchQuery by viewModel.query.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(WeatherTheme.colors.cardSurfaceFaint)
                .padding(20.dp),
    ) {
        WeatherTextField(
            value = searchQuery,
            onValueChange = viewModel::onQueryChange,
            label = { WeatherText(text = stringResource(id = R.string.city_search)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    },
                ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(cityState) { city ->
                CityCard(
                    city = city,
                    onCitySelected = {
                        viewModel.selectCity(it)
                        onCitySelected(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun CityCard(
    city: City,
    onCitySelected: (City) -> Unit,
) {
    WeatherCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onCitySelected(city) },
        containerColor = WeatherTheme.colors.citySurface,
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            WeatherText(
                text = listOfNotNull(city.name, city.admin1, city.country).joinToString(" - "),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
