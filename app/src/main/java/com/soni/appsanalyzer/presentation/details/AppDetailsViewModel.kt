package com.soni.appsanalyzer.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soni.appsanalyzer.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AppDetailsState>(AppDetailsState.Loading)
    val state = _state.asStateFlow()

    fun handleIntent(intent: AppDetailsIntent) {
        when (intent) {
            is AppDetailsIntent.LoadDetails -> loadDetails(intent.packageName)
        }
    }

    private fun loadDetails(packageName: String) {
        viewModelScope.launch {
            _state.value = AppDetailsState.Loading
            try {
                val details = repository.getAppDetails(packageName)
                _state.value = AppDetailsState.Success(details)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = AppDetailsState.Error(e.message ?: "Failed to load app details")
            }
        }
    }
}
