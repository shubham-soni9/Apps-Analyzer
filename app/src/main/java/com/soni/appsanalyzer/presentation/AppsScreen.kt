package com.soni.appsanalyzer.presentation

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.model.AppType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
        state: AppsContract.State,
        onIntent: (AppsContract.Intent) -> Unit,
        onNavigateToDetails: (String) -> Unit
) {
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        var sortAscending by remember { mutableStateOf(true) }

        val filteredApps =
                remember(state.apps, searchQuery, sortAscending) {
                        state.apps
                                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                                .sortedBy { if (sortAscending) it.name else null }
                                .let { if (!sortAscending) it.reversed() else it }
                }

        Scaffold(
                topBar = {
                        Column {
                                CenterAlignedTopAppBar(
                                        title = {
                                                Text(
                                                        "Apps Analyzer",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 22.sp
                                                )
                                        },
                                        colors =
                                                TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface,
                                                        titleContentColor =
                                                                MaterialTheme.colorScheme.onSurface
                                                )
                                )

                                AnimatedVisibility(
                                        visible = isSearchActive,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                ) {
                                        SearchBar(
                                                query = searchQuery,
                                                onQueryChange = { searchQuery = it },
                                                onClear = { searchQuery = "" }
                                        )
                                }

                                Divider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                )
                        }
                }
        ) { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

                        // Stats Card
                        StatsCard(stats = state.appStats)

                        // Filter Section with enhanced design
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                AppsContract.AppFilter.values().forEach { filter ->
                                        EnhancedFilterChip(
                                                filter = filter,
                                                isSelected = state.selectedFilter == filter,
                                                onClick = {
                                                        onIntent(
                                                                AppsContract.Intent.SelectFilter(
                                                                        filter
                                                                )
                                                        )
                                                }
                                        )
                                }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                when {
                                        state.isLoading -> {
                                                LoadingState()
                                        }
                                        state.error != null -> {
                                                ErrorState(error = state.error)
                                        }
                                        filteredApps.isEmpty() -> {
                                                EmptyState(hasSearch = searchQuery.isNotEmpty())
                                        }
                                        else -> {
                                                LazyColumn(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentPadding =
                                                                PaddingValues(
                                                                        horizontal = 16.dp,
                                                                        vertical = 8.dp
                                                                ),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        items(
                                                                items = filteredApps,
                                                                key = { it.packageName }
                                                        ) { app ->
                                                                EnhancedAppItem(
                                                                        app = app,
                                                                        onNavigateToDetails =
                                                                                onNavigateToDetails
                                                                )
                                                        }

                                                        item {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        16.dp
                                                                                )
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
        OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                        if (query.isNotEmpty()) {
                                IconButton(onClick = onClear) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                        }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
        )
}

@Composable
fun StatsCard(stats: AppsContract.AppStats) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        StatItem(
                                label = "All",
                                count = stats.totalCount,
                                total = stats.totalCount,
                                modifier = Modifier.weight(1f)
                        )
                        StatItem(
                                label = "Flutter",
                                count = stats.flutterCount,
                                total = stats.totalCount,
                                modifier = Modifier.weight(1f)
                        )
                        StatItem(
                                label = "React Native",
                                count = stats.reactNativeCount,
                                total = stats.totalCount,
                                modifier = Modifier.weight(1f)
                        )
                        StatItem(
                                label = "Native",
                                count = stats.nativeCount,
                                total = stats.totalCount,
                                modifier = Modifier.weight(1f)
                        )
                }
        }
}

@Composable
private fun getAppTypeStyle(appType: AppType): Pair<Color, Color> {
        return when (appType) {
                AppType.FLUTTER ->
                        Pair(
                                Color(0xFFE3FFD), // Light Blue
                                Color(0xFF1565C0) // Dark Blue
                        )
                AppType.REACT_NATIVE, AppType.REACT_NATIVE_EXPO ->
                        Pair(
                                Color(0xFFE0F7FA), // Light Cyan
                                Color(0xFF006064) // Dark Cyan
                        )
                AppType.KMM ->
                        Pair(
                                Color(0xFFF3E5F5), // Light Purple
                                Color(0xFF4A148C) // Dark Purple
                        )
                AppType.NATIVE ->
                        Pair(
                                Color(0xFFE8F5E9), // Light Green
                                Color(0xFF1B5E20) // Dark Green
                        )
                AppType.UNKNOWN ->
                        Pair(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
        }
}

@Composable
fun StatItem(label: String, count: Int, total: Int, modifier: Modifier = Modifier) {
        val percentage = if (total > 0) (count.toFloat() / total) * 100 else 0f

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
                Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
                Text(
                        text = String.format("%.1f%%", percentage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 10.sp
                )
        }
}

@Composable
fun EnhancedFilterChip(filter: AppsContract.AppFilter, isSelected: Boolean, onClick: () -> Unit) {
        val scale by
                animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

        FilterChip(
                selected = isSelected,
                onClick = onClick,
                label = {
                        Text(
                                filter.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                },
                leadingIcon =
                        if (isSelected) {
                                {
                                        Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                }
                        } else null,
                modifier = Modifier.scale(scale),
                colors =
                        FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                border =
                        FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor =
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                        )
        )
}

@Composable
fun LoadingState() {
        Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp), strokeWidth = 4.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = "Loading apps...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
}

@Composable
fun ErrorState(error: String) {
        Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = "Oops! Something went wrong",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
}

@Composable
fun EmptyState(hasSearch: Boolean) {
        Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Icon(
                        imageVector = if (hasSearch) Icons.Default.Search else Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = if (hasSearch) "No apps found" else "No apps available",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = if (hasSearch) "Try adjusting your search" else "Pull to refresh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                                                context.packageManager.getApplicationIcon(
                                                        packageName
                                                )
                                        } catch (e: Exception) {
                                                null
                                        }
                                }
                }

        Box(
                modifier =
                        modifier.clip(RoundedCornerShape(12.dp))
                                .background(
                                        if (icon == null) {
                                                Brush.linearGradient(
                                                        colors =
                                                                listOf(
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer,
                                                                        MaterialTheme.colorScheme
                                                                                .tertiaryContainer
                                                                )
                                                )
                                        } else {
                                                Brush.linearGradient(
                                                        colors =
                                                                listOf(
                                                                        Color.Transparent,
                                                                        Color.Transparent
                                                                )
                                                )
                                        }
                                )
        ) {
                if (icon != null) {
                        Image(
                                bitmap = icon!!.toBitmap().asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                        )
                } else {
                        Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center).size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAppItem(app: AppInfo, onNavigateToDetails: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }

        Card(
                onClick = { onNavigateToDetails(app.packageName) },
                elevation =
                        CardDefaults.cardElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 8.dp
                        ),
                modifier = Modifier.fillMaxWidth(),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                shape = RoundedCornerShape(16.dp)
        ) {
                Column {
                        Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                AppIcon(
                                        packageName = app.packageName,
                                        modifier = Modifier.size(56.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = app.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                val (backgroundColor, contentColor) =
                                                        getAppTypeStyle(app.appType)

                                                Surface(
                                                        color = backgroundColor,
                                                        shape = RoundedCornerShape(6.dp)
                                                ) {
                                                        Text(
                                                                text = app.appType.displayName,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelSmall,
                                                                fontWeight = FontWeight.Medium,
                                                                color = contentColor,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 8.dp,
                                                                                vertical = 4.dp
                                                                        )
                                                        )
                                                }

                                                Text(
                                                        text = "v${app.versionName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }
                        }

                        AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                        ) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .background(
                                                                MaterialTheme.colorScheme.surface
                                                                        .copy(alpha = 0.5f)
                                                        )
                                                        .padding(16.dp)
                                ) {
                                        Divider(
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        DetailRow(
                                                icon = Icons.Outlined.Code,
                                                label = "Package",
                                                value = app.packageName
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        DetailRow(
                                                icon = Icons.Outlined.Info,
                                                label = "Version",
                                                value = "${app.versionName} (${app.versionCode})"
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                OutlinedButton(
                                                        onClick = { /* Open app */},
                                                        modifier = Modifier.weight(1f)
                                                ) {
                                                        Icon(
                                                                Icons.Default.OpenInNew,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Open")
                                                }

                                                OutlinedButton(
                                                        onClick = { /* Share */},
                                                        modifier = Modifier.weight(1f)
                                                ) {
                                                        Icon(
                                                                Icons.Default.Share,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Share")
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                        )
                }
        }
}
