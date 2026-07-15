package com.erendogan6.havatahminim.di

import android.app.Application
import com.erendogan6.havatahminim.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Debug builds get the auto-tagging tree; release plants nothing, so every Timber call
        // is a no-op there. JVM unit tests also plant nothing — which is exactly what lets the
        // tested modules live without returnDefaultValues (no android.util.Log is ever touched).
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
