package com.soni.appsanalyzer.domain.usecase

import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.repository.AppRepository

class GetInstalledAppsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(): List<AppInfo> {
        return repository.getInstalledApps()
    }
}
