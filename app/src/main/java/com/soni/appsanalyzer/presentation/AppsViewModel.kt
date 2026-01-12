package com.soni.appsanalyzer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soni.appsanalyzer.domain.usecase.GetInstalledAppsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppsViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AppsContract.State())
    val state: StateFlow<AppsContract.State> = _state.asStateFlow()

    private val _effect = Channel<AppsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(AppsContract.Intent.LoadApps)
    }

    fun handleIntent(intent: AppsContract.Intent) {
        when (intent) {
            is AppsContract.Intent.LoadApps -> loadApps()
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val apps = getInstalledAppsUseCase()
                _state.update { it.copy(apps = apps, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.send(AppsContract.Effect.ShowToast(e.message ?: "Unknown error"))
            }
        }
    }
}
