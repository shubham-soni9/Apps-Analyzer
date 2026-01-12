package com.soni.appsanalyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
