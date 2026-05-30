package com.erendogan6.havatahminim.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.util.Converters

@Database(
    entities = [
        LocationEntity::class,
        DailyForecastEntity::class,
        WeatherSuggestionEntity::class,
        AllergenPreferenceEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun dailyForecastDao(): DailyForecastDao

    abstract fun weatherSuggestionDao(): WeatherSuggestionDao

    abstract fun allergenPreferenceDao(): AllergenPreferenceDao
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

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM `daily_forecast`")
            db.execSQL("DELETE FROM `weather_suggestions`")
        }
    }

/** Adds the allergen personalization table (which pollens the user is sensitive to). */
val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `allergen_preferences` (" +
                    "`type` TEXT PRIMARY KEY NOT NULL, " +
                    "`sensitive` INTEGER NOT NULL)",
            )
        }
    }

/** Records the language a cached ZekAI suggestion was generated for, so a language change refreshes it. */
val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `weather_suggestions` ADD COLUMN `language` TEXT NOT NULL DEFAULT ''")
        }
    }
