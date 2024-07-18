package com.erendogan6.havatahminim.room

import androidx.room.TypeConverter
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
