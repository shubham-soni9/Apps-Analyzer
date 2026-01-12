package com.soni.appsanalyzer.domain.usecase

import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.repository.AppRepository
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(private val repository: AppRepository) {
    suspend operator fun invoke(): List<AppInfo> {
        return repository.getInstalledApps()
    }
}
