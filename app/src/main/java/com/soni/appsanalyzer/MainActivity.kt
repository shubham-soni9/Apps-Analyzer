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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.soni.appsanalyzer.presentation.AppsContract
import com.soni.appsanalyzer.presentation.AppsScreen
import com.soni.appsanalyzer.presentation.AppsViewModel
import com.soni.appsanalyzer.presentation.details.AppDetailsScreen
import com.soni.appsanalyzer.ui.theme.AppsAnalyzerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppsAnalyzerTheme {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "apps_list") {
                        composable("apps_list") {
                            val viewModel: AppsViewModel = hiltViewModel()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collectLatest { effect ->
                                    when (effect) {
                                        is AppsContract.Effect.ShowToast -> {
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            effect.message,
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                    }
                                }
                            }

                            AppsScreen(
                                    state = state,
                                    onIntent = viewModel::handleIntent,
                                    onNavigateToDetails = { packageName ->
                                        navController.navigate("app_details/$packageName")
                                    }
                            )
                        }

                        composable(
                                route = "app_details/{packageName}",
                                arguments =
                                        listOf(
                                                navArgument("packageName") {
                                                    type = NavType.StringType
                                                }
                                        )
                        ) { backStackEntry ->
                            val packageName =
                                    backStackEntry.arguments?.getString("packageName")
                                            ?: return@composable
                            AppDetailsScreen(
                                    packageName = packageName,
                                    onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
