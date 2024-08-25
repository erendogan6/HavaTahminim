package com.erendogan6.havatahminim.di

import android.content.Context
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.erendogan6.havatahminim.BuildConfig
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.ProWeatherApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.room.MIGRATION_1_2
import com.erendogan6.havatahminim.room.RoomDB
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import com.erendogan6.havatahminim.util.ResourcesProvider
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
object AppModule {
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
    fun provideWeatherApiServiceSecure(okHttpClient: OkHttpClient): WeatherApiService =
        Retrofit
            .Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideWeatherApiServiceUnSecure(okHttpClient: OkHttpClient): CityApiService =
        Retrofit
            .Builder()
            .baseUrl("http://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CityApiService::class.java)

    @Provides
    @Singleton
    fun provideProWeatherApiService(okHttpClient: OkHttpClient): ProWeatherApiService =
        Retrofit
            .Builder()
            .baseUrl("https://pro.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProWeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideGeminiService(resourcesProvider: ResourcesProvider): GeminiService = GeminiService(resourcesProvider)

    @Provides
    @Singleton
    fun provideResourcesProvider(
        @ApplicationContext context: Context,
    ): ResourcesProvider = ResourcesProvider(context)

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherApiService: WeatherApiService,
        proWeatherApiService: ProWeatherApiService,
        geminiService: GeminiService,
        cityApiService: CityApiService,
        locationDao: LocationDao,
        dailyForecastDao: DailyForecastDao,
        resourcesProvider: ResourcesProvider,
        weatherSuggestionDao: WeatherSuggestionDao,
    ): WeatherRepository =
        WeatherRepository(
            weatherApiService,
            proWeatherApiService,
            geminiService,
            cityApiService,
            locationDao,
            dailyForecastDao,
            resourcesProvider,
            weatherSuggestionDao,
        )

    @Provides
    @Singleton
    fun provideLocationDatabase(
        @ApplicationContext
        context: Context,
    ): RoomDB =
        Room
            .databaseBuilder(
                context.applicationContext,
                RoomDB::class.java,
                "location_database",
            ).addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideLocationDao(roomDB: RoomDB): LocationDao = roomDB.locationDao()

    @Provides
    @Singleton
    fun provideDailyForecastDao(roomDb: RoomDB): DailyForecastDao = roomDb.dailyForecastDao()

    @Provides
    @Singleton
    fun provideWeatherSuggestionDao(roomDb: RoomDB): WeatherSuggestionDao = roomDb.weatherSuggestionDao()
}
