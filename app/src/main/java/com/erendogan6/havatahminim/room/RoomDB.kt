package com.erendogan6.havatahminim.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.util.Converters

@Database(entities = [LocationEntity::class, DailyForecastEntity::class, WeatherSuggestionEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun dailyForecastDao(): DailyForecastDao

    abstract fun weatherSuggestionDao(): WeatherSuggestionDao
}

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `weather_suggestions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`location` TEXT NOT NULL, " +
                    "`temperature` TEXT NOT NULL, " +
                    "`suggestion` TEXT NOT NULL, " +
                    "`latitude` REAL NOT NULL, " +
                    "`longitude` REAL NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL)",
            )
        }
    }
