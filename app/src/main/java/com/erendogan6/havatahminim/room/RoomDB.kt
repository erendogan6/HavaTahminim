package com.erendogan6.havatahminim.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
abstract class RoomDB : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
