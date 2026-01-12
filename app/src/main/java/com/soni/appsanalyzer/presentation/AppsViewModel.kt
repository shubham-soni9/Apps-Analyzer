package com.soni.appsanalyzer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.model.AppType
import com.soni.appsanalyzer.domain.usecase.GetInstalledAppsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppsViewModel(private val getInstalledAppsUseCase: GetInstalledAppsUseCase) : ViewModel() {

    private val _state = MutableStateFlow(AppsContract.State())
    val state: StateFlow<AppsContract.State> = _state.asStateFlow()

    private val _effect = Channel<AppsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var allApps: List<AppInfo> = emptyList()

    init {
        handleIntent(AppsContract.Intent.LoadApps)
    }

    fun handleIntent(intent: AppsContract.Intent) {
        when (intent) {
            is AppsContract.Intent.LoadApps -> loadApps()
            is AppsContract.Intent.SelectFilter -> selectFilter(intent.filter)
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                allApps = getInstalledAppsUseCase()
                applyFilter(_state.value.selectedFilter)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.send(AppsContract.Effect.ShowToast(e.message ?: "Unknown error"))
            }
        }
    }

    private fun selectFilter(filter: AppsContract.AppFilter) {
        applyFilter(filter)
    }

    private fun applyFilter(filter: AppsContract.AppFilter) {
        val filteredApps =
                when (filter) {
                    AppsContract.AppFilter.ALL -> allApps
                    AppsContract.AppFilter.NATIVE -> allApps.filter { it.appType == AppType.NATIVE }
                    AppsContract.AppFilter.FLUTTER ->
                            allApps.filter { it.appType == AppType.FLUTTER }
                    AppsContract.AppFilter.REACT_NATIVE ->
                            allApps.filter {
                                it.appType == AppType.REACT_NATIVE ||
                                        it.appType == AppType.REACT_NATIVE_EXPO
                            }
                }
        _state.update { it.copy(apps = filteredApps, isLoading = false, selectedFilter = filter) }
    }
}
