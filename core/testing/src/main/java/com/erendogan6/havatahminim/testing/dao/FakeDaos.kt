package com.erendogan6.havatahminim.testing.dao

import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.room.AllergenPreferenceDao
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.yield

class FakeLocationDao : LocationDao {
    var stored: LocationEntity? = null

    /** Thrown once by the next insert — exercises best-effort persistence paths. */
    var insertError: Exception? = null

    override suspend fun getLocation(): LocationEntity? {
        yield()
        return stored
    }

    override suspend fun insertLocation(location: LocationEntity): Long {
        yield()
        insertError?.let {
            insertError = null
            throw it
        }
        stored = location
        return 1L
    }
}

class FakeDailyForecastDao : DailyForecastDao {
    val byDate = mutableMapOf<Long, DailyForecastEntity>()
    var insertCount = 0
        private set

    override suspend fun getForecastByDate(date: Long): DailyForecastEntity? {
        yield()
        return byDate[date]
    }

    override suspend fun insertForecast(forecast: DailyForecastEntity) {
        yield()
        insertCount++
        byDate[forecast.date] = forecast
    }
}

class FakeWeatherSuggestionDao : WeatherSuggestionDao {
    var stored: WeatherSuggestionEntity? = null
    var deleteAllCount = 0
        private set

    override suspend fun insertSuggestion(suggestion: WeatherSuggestionEntity) {
        yield()
        stored = suggestion
    }

    override suspend fun getLatestSuggestion(): WeatherSuggestionEntity? {
        yield()
        return stored
    }

    override suspend fun deleteAllSuggestions() {
        yield()
        deleteAllCount++
        stored = null
    }
}

class FakeAllergenPreferenceDao : AllergenPreferenceDao {
    private val rows = MutableStateFlow<Map<String, AllergenPreferenceEntity>>(emptyMap())

    /** Thrown once by the next write — exercises best-effort persistence paths. */
    var setError: Exception? = null

    fun seed(vararg entities: AllergenPreferenceEntity) {
        rows.value = entities.associateBy { it.type }
    }

    override fun getAll(): Flow<List<AllergenPreferenceEntity>> = rows.map { it.values.toList() }

    override suspend fun getSensitive(): List<AllergenPreferenceEntity> {
        yield()
        return rows.value.values.filter { it.sensitive }
    }

    override suspend fun setPreference(preference: AllergenPreferenceEntity) {
        yield()
        setError?.let {
            setError = null
            throw it
        }
        rows.value = rows.value + (preference.type to preference)
    }
}
