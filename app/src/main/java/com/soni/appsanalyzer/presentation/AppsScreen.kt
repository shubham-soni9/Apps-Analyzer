package com.soni.appsanalyzer.presentation

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.soni.appsanalyzer.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(state: AppsContract.State, onIntent: (AppsContract.Intent) -> Unit) {
    Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                        title = { Text("Installed Apps") },
                        actions = {
                            IconButton(onClick = { onIntent(AppsContract.Intent.SyncApps) }) {
                                Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // Filter Section
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppsContract.AppFilter.values().forEach { filter ->
                    FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { onIntent(AppsContract.Intent.SelectFilter(filter)) },
                            label = { Text(filter.displayName) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.error != null) {
                    Text(
                            text = state.error ?: "Unknown Error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.apps,
                            key = { it.packageName }
                        ) { app -> AppItem(app) }
                    }
                }
            }
        }
    }
}

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon by
            produceState<Drawable?>(initialValue = null, key1 = packageName) {
                value =
                        withContext(Dispatchers.IO) {
                            try {
                                context.packageManager.getApplicationIcon(packageName)
                            } catch (e: Exception) {
                                null
                            }
                        }
            }

    if (icon != null) {
        Image(
                bitmap = icon!!.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppItem(app: AppInfo) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
    ) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = app.packageName, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = app.name, style = MaterialTheme.typography.titleMedium)
                Text(text = app.packageName, style = MaterialTheme.typography.bodySmall)
                Text(text = "v${app.versionName}", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = app.appType.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
