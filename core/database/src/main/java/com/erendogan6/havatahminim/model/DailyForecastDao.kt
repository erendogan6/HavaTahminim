package com.erendogan6.havatahminim.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity

@Dao
interface DailyForecastDao {
    @Query("SELECT * FROM daily_forecast WHERE date = :date")
    suspend fun getForecastByDate(date: Long): DailyForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: DailyForecastEntity)
}
