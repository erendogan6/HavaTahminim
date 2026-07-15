package com.erendogan6.havatahminim.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse

/** One cached day of forecast, keyed by [date]; reused while the location stays within 10 km. */
@Entity(tableName = "daily_forecast")
data class DailyForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val latitude: Double,
    val longitude: Double,
    val forecastData: DailyForecastBaseResponse,
)
