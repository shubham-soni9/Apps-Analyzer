package com.soni.appsanalyzer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps") fun getAllApps(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertApps(apps: List<AppEntity>)

    @Query("DELETE FROM apps") suspend fun clearApps()
}
