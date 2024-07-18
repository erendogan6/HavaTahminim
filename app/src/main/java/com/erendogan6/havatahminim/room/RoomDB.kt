package com.erendogan6.havatahminim.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.database.DailyForecastEntity
import com.erendogan6.havatahminim.model.database.LocationEntity

@Database(entities = [LocationEntity::class, DailyForecastEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun dailyForecastDao(): DailyForecastDao
}
