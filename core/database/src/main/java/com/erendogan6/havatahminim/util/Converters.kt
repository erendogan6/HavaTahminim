package com.erendogan6.havatahminim.util

import androidx.room.TypeConverter
import com.erendogan6.havatahminim.model.weather.dailyforecast.DailyForecastBaseResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Gson-serializes the daily forecast into a single TEXT column. */
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromDailyForecastBaseResponse(value: DailyForecastBaseResponse): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDailyForecastBaseResponse(value: String): DailyForecastBaseResponse {
        val type = object : TypeToken<DailyForecastBaseResponse>() {}.type
        return gson.fromJson(value, type)
    }
}
