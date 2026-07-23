package com.erendogan6.havatahminim.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.erendogan6.havatahminim.util.installJankStats
import dagger.hilt.android.AndroidEntryPoint

/** Single activity; everything lives in [HavaTahminimApp]. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installJankStats()
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
        )
        setContent {
            HavaTahminimApp()
        }
    }
}
