package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.android.tools.screenshot.PreviewTest

/*
 * Golden corpus for the design-system base components: each @Preview here is rendered by
 * validateDebugScreenshotTest and compared against src/debugScreenshotTest/reference/.
 * Regenerate with updateDebugScreenshotTest after an intentional visual change.
 */

private const val SKY_BACKGROUND = 0xFF6FA8DC

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun WeatherTextPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherText(text = "Parçalı bulutlu", style = MaterialTheme.typography.headlineMedium)
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun WeatherCardPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherCard(modifier = Modifier.padding(16.dp)) {
            WeatherText(text = "Bugün", modifier = Modifier.padding(16.dp))
        }
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun WeatherCardClickablePreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherCard(onClick = {}, modifier = Modifier.padding(16.dp)) {
            WeatherText(text = "Tıklanabilir kart", modifier = Modifier.padding(16.dp))
        }
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun WeatherTextFieldPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherTextField(
            value = "İstanbul",
            onValueChange = {},
            label = { WeatherText(text = "Şehir ara") },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTest
@Preview
@Composable
fun WeatherDialogPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherDialog(
            title = "İnternet Bağlantısı Yok",
            message = "Hava durumu verilerini almak için internet bağlantısına ihtiyacınız var.",
            confirmText = "Tamam",
            onConfirm = {},
            onDismissRequest = {},
            dismissText = "Vazgeç",
            onDismiss = {},
        )
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND)
@Composable
fun WeatherIconButtonPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherIconButton(icon = Icons.Default.Search, contentDescription = "Ara", onClick = {})
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = SKY_BACKGROUND, heightDp = 200)
@Composable
fun CenteredColumnPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        CenteredColumn(modifier = Modifier.height(200.dp)) {
            WeatherText(text = "Ortalanmış içerik")
        }
    }
}
