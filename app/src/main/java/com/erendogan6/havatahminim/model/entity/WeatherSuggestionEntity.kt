package com.erendogan6.havatahminim.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_suggestions")
data class WeatherSuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val location: String,
    val temperature: String,
    val suggestion: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
