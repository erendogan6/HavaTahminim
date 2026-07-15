package com.erendogan6.havatahminim.di

import android.app.Application
import com.erendogan6.havatahminim.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/** Hilt root. Also plants the app's only Timber tree (debug builds). */
@HiltAndroidApp
class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Only debug builds get a tree; with nothing planted, Timber calls are no-ops in
        // release builds and in JVM unit tests (no android.util.Log involved).
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
