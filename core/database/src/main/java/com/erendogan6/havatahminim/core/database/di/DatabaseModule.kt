package com.erendogan6.havatahminim.core.database.di

import android.content.Context
import androidx.room.Room
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.room.AllergenPreferenceDao
import com.erendogan6.havatahminim.room.MIGRATION_1_2
import com.erendogan6.havatahminim.room.MIGRATION_2_3
import com.erendogan6.havatahminim.room.MIGRATION_3_4
import com.erendogan6.havatahminim.room.MIGRATION_4_5
import com.erendogan6.havatahminim.room.RoomDB
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
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
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

    @Provides
    @Singleton
    fun provideAllergenPreferenceDao(roomDb: RoomDB): AllergenPreferenceDao = roomDb.allergenPreferenceDao()
}
