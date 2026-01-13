package com.soni.appsanalyzer.domain.usecase

import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(private val repository: AppRepository) {
    operator fun invoke(): Flow<List<AppInfo>> {
        return repository.getInstalledApps()
    }
}
