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
import com.erendogan6.havatahminim.ui.component.WeatherText
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.ui.component.CenteredColumn
import com.erendogan6.havatahminim.ui.theme.WeatherTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.erendogan6.havatahminim.ui.viewModel.ZekAiViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ZekAIScreen(
    modifier: Modifier = Modifier,
    viewModel: ZekAiViewModel = hiltViewModel(),
) {
    // Subscribing IS the trigger: the ViewModel's WhileSubscribed pipeline generates/refreshes
    // the suggestion whenever this tab is open and its inputs (location, allergens) changed.
    val weatherSuggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background.copy(alpha = 0f)) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.size(30.dp))
            weatherSuggestions?.let { SuggestionsCard(it) } ?: ThinkerCard()
        }
    }
}

@Composable
private fun SuggestionsCard(suggestions: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.zekai),
            contentDescription = null,
            modifier =
                Modifier
                    .size(260.dp)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        WeatherText(
            text = stringResource(id = R.string.zekai_suggestions),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    WeatherTheme.colors.cardSurface,
                    RoundedCornerShape(12.dp),
                ).padding(16.dp),
    ) {
        MarkdownText(
            markdown = suggestions,
            modifier = Modifier.padding(vertical = 4.dp),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    shadow = Shadow(color = WeatherTheme.colors.shadowSoft, blurRadius = 2f),
                    color = WeatherTheme.colors.onCard,
                    fontSize = 21.sp,
                ),
        )
    }
}

@Composable
private fun ThinkerCard() {
    CenteredColumn {
        CircularProgressIndicator()
        WeatherText(
            text = stringResource(id = R.string.zekai_thinking),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}
