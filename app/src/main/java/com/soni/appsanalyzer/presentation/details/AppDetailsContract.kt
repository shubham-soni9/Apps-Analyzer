package com.soni.appsanalyzer.presentation.details

import com.soni.appsanalyzer.domain.model.AppDetailInfo

sealed interface AppDetailsState {
    data object Loading : AppDetailsState
    data class Success(val appDetail: AppDetailInfo) : AppDetailsState
    data class Error(val message: String) : AppDetailsState
}

sealed interface AppDetailsIntent {
    data class LoadDetails(val packageName: String) : AppDetailsIntent
}
