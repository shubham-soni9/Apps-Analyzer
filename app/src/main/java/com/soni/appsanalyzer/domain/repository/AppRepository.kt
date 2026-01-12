package com.soni.appsanalyzer.domain.repository

import com.soni.appsanalyzer.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): Flow<List<AppInfo>>
    suspend fun syncApps()
}
