package com.soni.appsanalyzer.domain.usecase

import com.soni.appsanalyzer.domain.repository.AppRepository
import javax.inject.Inject

class SyncAppsUseCase @Inject constructor(private val repository: AppRepository) {
    suspend operator fun invoke() {
        repository.syncApps()
    }
}
