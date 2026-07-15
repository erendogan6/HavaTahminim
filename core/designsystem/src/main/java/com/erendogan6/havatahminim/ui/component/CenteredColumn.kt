package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme

/** Full-size column centering its content both ways (splash, loading, error states). */
@Composable
fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF6FA8DC, heightDp = 200)
@Composable
private fun CenteredColumnPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        CenteredColumn(modifier = Modifier.height(200.dp)) {
            WeatherText(text = "Ortalanmış içerik")
        }
    }
}
