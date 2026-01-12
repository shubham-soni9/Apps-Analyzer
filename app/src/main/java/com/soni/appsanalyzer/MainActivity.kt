package com.soni.appsanalyzer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soni.appsanalyzer.data.repository.AppRepositoryImpl
import com.soni.appsanalyzer.domain.usecase.GetInstalledAppsUseCase
import com.soni.appsanalyzer.presentation.AppsContract
import com.soni.appsanalyzer.presentation.AppsScreen
import com.soni.appsanalyzer.presentation.AppsViewModel
import com.soni.appsanalyzer.ui.theme.AppsAnalyzerTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val appRepository = AppRepositoryImpl(applicationContext)
        val getInstalledAppsUseCase = GetInstalledAppsUseCase(appRepository)
        
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppsViewModel(getInstalledAppsUseCase) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[AppsViewModel::class.java]

        setContent {
            AppsAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    
                    LaunchedEffect(Unit) {
                        viewModel.effect.collectLatest { effect ->
                            when(effect) {
                                is AppsContract.Effect.ShowToast -> {
                                    Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    AppsScreen(
                        state = state,
                        onIntent = viewModel::handleIntent
                    )
                }
            }
        }
    }
}
