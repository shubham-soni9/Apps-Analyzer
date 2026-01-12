package com.soni.appsanalyzer.domain.repository

import com.soni.appsanalyzer.domain.model.AppInfo

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
}
