package com.soni.appsanalyzer.presentation

import com.soni.appsanalyzer.domain.model.AppInfo

class AppsContract {

    enum class AppFilter(val displayName: String) {
        ALL("All"),
        NATIVE("Native"),
        REACT_NATIVE("React Native"),
        FLUTTER("Flutter")
    }

    data class State(
            val apps: List<AppInfo> = emptyList(),
            val isLoading: Boolean = false,
            val error: String? = null,
            val selectedFilter: AppFilter = AppFilter.ALL
    )

    sealed class Intent {
        object LoadApps : Intent()
        object SyncApps : Intent()
        data class SelectFilter(val filter: AppFilter) : Intent()
    }

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
    }
}
