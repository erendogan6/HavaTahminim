package com.erendogan6.havatahminim.core.network.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.erendogan6.havatahminim.core.network.BuildConfig
import com.erendogan6.havatahminim.network.AirQualityApiService
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logging =
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            builder.addInterceptor(logging)

            val chuckerInterceptor = ChuckerInterceptor.Builder(context).build()
            builder.addInterceptor(chuckerInterceptor)
        }

        builder.connectTimeout(30, TimeUnit.SECONDS)
        builder.readTimeout(30, TimeUnit.SECONDS)
        builder.writeTimeout(30, TimeUnit.SECONDS)

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(okHttpClient: OkHttpClient): WeatherApiService =
        Retrofit
            .Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideCityApiService(okHttpClient: OkHttpClient): CityApiService =
        Retrofit
            .Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CityApiService::class.java)

    @Provides
    @Singleton
    fun provideAirQualityApiService(okHttpClient: OkHttpClient): AirQualityApiService =
        Retrofit
            .Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirQualityApiService::class.java)
}
