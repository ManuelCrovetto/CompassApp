package com.macrosystems.compassapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NavigationDetailsEntity::class], version = 1)
abstract class NavigationDetailsDatabase: RoomDatabase() {

    abstract fun navigationDetailsDao(): NavigationDetailsDao

}