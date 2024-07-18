package com.erendogan6.havatahminim.di

import com.erendogan6.havatahminim.BuildConfig
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.ProWeatherApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import com.erendogan6.havatahminim.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        builder.connectTimeout(30, TimeUnit.SECONDS)
        builder.readTimeout(30, TimeUnit.SECONDS)
        builder.writeTimeout(30, TimeUnit.SECONDS)

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiServiceSecure(okHttpClient: OkHttpClient): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApiServiceUnSecure(okHttpClient: OkHttpClient): CityApiService {
        return Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CityApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProWeatherApiService(okHttpClient: OkHttpClient): ProWeatherApiService {
        return Retrofit.Builder()
            .baseUrl("https://pro.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProWeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService()
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherApiService: WeatherApiService,
        proWeatherApiService: ProWeatherApiService,
        geminiService: GeminiService,
        cityApiService: CityApiService
    ): WeatherRepository {
        return WeatherRepository(weatherApiService, proWeatherApiService,geminiService,cityApiService)
    }
}
