package com.erendogan6.havatahminim.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity

@Dao
interface WeatherSuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: WeatherSuggestionEntity)

    @Query("SELECT * FROM weather_suggestions LIMIT 1")
    suspend fun getLatestSuggestion(): WeatherSuggestionEntity?

    @Query("DELETE FROM weather_suggestions")
    suspend fun deleteAllSuggestions()
}
