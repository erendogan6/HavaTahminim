package com.erendogan6.havatahminim.di

import android.app.Application
import com.erendogan6.havatahminim.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/** Hilt application; plants the Timber tree for debug builds. */
@HiltAndroidApp
class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Nothing is planted in release builds or JVM tests, so logging is a no-op there.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
