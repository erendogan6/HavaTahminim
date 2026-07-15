package com.erendogan6.havatahminim.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The saved location row; the table holds at most one. */
@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
)
