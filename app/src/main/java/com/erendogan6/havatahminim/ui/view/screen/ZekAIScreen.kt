package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.ui.viewModel.WeatherViewModel

@Composable
fun ZekAIScreen(weatherViewModel: WeatherViewModel) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val weatherSuggestions by weatherViewModel.weatherSuggestions.collectAsState()
    var isLoadingSuggestions by remember { mutableStateOf(true) }

    WeatherBackgroundLayout(weatherState) {
        Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.size(30.dp))
                weatherSuggestions?.let {
                    SuggestionsCard(it)
                    isLoadingSuggestions = false
                } ?: ThinkerCard()
            }
        }
    }
}

@Composable
fun SuggestionsCard(suggestions: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.zekai),
            contentDescription = null,
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(id = R.string.zekai_suggestions),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
            style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xAA80C4E9), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                val parts = suggestions.split("**")
                parts.forEachIndexed { index, part ->
                    if (index % 2 == 1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(part)
                        }
                    } else {
                        val subParts = part.split("*")
                        subParts.forEachIndexed { subIndex, subPart ->
                            if (subIndex % 2 == 1) {
                                append("\n\t-$subPart")
                            } else {
                                append(subPart)
                            }
                        }
                    }
                }
            },
            fontSize = 21.sp,
            modifier = Modifier.padding(vertical = 4.dp),
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.open_sans)),
            style = TextStyle(shadow = Shadow(color = Color.Gray, blurRadius = 2f))
        )
    }
}

@Composable
fun ThinkerCard() {
    CenteredColumn {
        CircularProgressIndicator()
        Text(
            text = stringResource(id = R.string.zekai_thinking),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
            style = TextStyle(shadow = Shadow(color = Color.DarkGray, blurRadius = 2f))
        )
    }
}
