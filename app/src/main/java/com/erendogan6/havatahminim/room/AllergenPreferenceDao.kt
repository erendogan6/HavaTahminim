package com.erendogan6.havatahminim.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergenPreferenceDao {
    @Query("SELECT * FROM allergen_preferences")
    fun getAll(): Flow<List<AllergenPreferenceEntity>>

    @Query("SELECT * FROM allergen_preferences WHERE sensitive = 1")
    suspend fun getSensitive(): List<AllergenPreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: AllergenPreferenceEntity)
}
