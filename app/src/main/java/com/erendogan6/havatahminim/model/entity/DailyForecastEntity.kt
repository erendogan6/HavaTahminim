package com.erendogan6.havatahminim.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse

@Entity(tableName = "daily_forecast")
data class DailyForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val latitude: Double,
    val longitude: Double,
    val forecastData: DailyForecastBaseResponse,
)
