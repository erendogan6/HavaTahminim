package com.erendogan6.havatahminim.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erendogan6.havatahminim.model.City
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel

@Composable
fun CitySearchScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    onCitySelected: (City) -> Unit = {}
) {
    val cityState by weatherViewModel.cities.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (searchQuery.length > 2) {
                    weatherViewModel.fetchCities(it)
                }
            },
            label = { Text("Şehir Ara") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
            items(cityState) { city ->
                Text(
                    text = city.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onCitySelected(city) }
                )
            }
        }
    }
}
