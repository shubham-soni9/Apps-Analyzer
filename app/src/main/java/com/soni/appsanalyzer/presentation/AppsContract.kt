package com.soni.appsanalyzer.presentation

import com.soni.appsanalyzer.domain.model.AppInfo

class AppsContract {
    data class State(
        val apps: List<AppInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed class Intent {
        object LoadApps : Intent()
    }

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
    }
}
