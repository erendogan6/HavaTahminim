package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel

@Composable
fun CitySearchScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    onCitySelected: (City) -> Unit = {},
) {
    val cityState by weatherViewModel.cities.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            weatherViewModel.clearCities()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xAA80C4E9).copy(alpha = 0.2f))
                .padding(20.dp),
    ) {
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (searchQuery.length > 2) {
                    weatherViewModel.fetchCities(it)
                }
            },
            label = { Text(text = stringResource(id = R.string.city_search)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray,
                ),
            shape = RoundedCornerShape(12.dp),
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
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(cityState) { city ->
                CityCard(city, onCitySelected)
            }
        }
    }
}

@Composable
fun CityCard(
    city: City,
    onCitySelected: (City) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCitySelected(city) }.shadow(4.dp, RoundedCornerShape(8.dp)),
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFFBBDEFB),
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text =
                    (
                        if (city.localNames?.tr != null) {
                            city.localNames.tr
                        } else {
                            city.name
                        }
                    ) + " - " + city.country,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        }
    }
}
