package com.erendogan6.havatahminim.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists which allergens the user marked as relevant. [type] is a [PollenType] name. Rows that
 * are absent (or `sensitive = false`) mean "not specifically sensitive". When the user has not
 * selected any, the app treats all allergens as relevant.
 */
@Entity(tableName = "allergen_preferences")
data class AllergenPreferenceEntity(
    @PrimaryKey val type: String,
    val sensitive: Boolean,
)
