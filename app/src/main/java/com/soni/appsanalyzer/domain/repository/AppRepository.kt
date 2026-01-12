package com.soni.appsanalyzer.domain.repository

import com.soni.appsanalyzer.domain.model.AppDetailInfo
import com.soni.appsanalyzer.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): Flow<List<AppInfo>>
    suspend fun syncApps()
    suspend fun getAppDetails(packageName: String): AppDetailInfo
}
