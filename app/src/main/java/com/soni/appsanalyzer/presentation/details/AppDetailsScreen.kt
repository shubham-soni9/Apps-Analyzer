package com.soni.appsanalyzer.presentation.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soni.appsanalyzer.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    onBackClick: () -> Unit,
    viewModel: AppDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(packageName) {
        viewModel.handleIntent(AppDetailsIntent.LoadDetails(packageName))
    }

    when (val s = state) {
        is AppDetailsState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AppDetailsState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is AppDetailsState.Success -> {
            AppDetailsContent(s.appDetail, onBackClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsContent(info: AppDetailInfo, onBackClick: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Analysis", "Components", "Permissions")

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = info.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = info.packageName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(info)
                1 -> AnalysisTab(info)
                2 -> ComponentsTab(info)
                3 -> PermissionsTab(info)
            }
        }
    }
}

@Composable
fun OverviewTab(info: AppDetailInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InfoCard("Installation") {
                InfoRow("Version Name", info.versionName)
                InfoRow("Version Code", info.versionCode.toString())
                InfoRow("Target SDK", "${info.targetSdk}")
                InfoRow("Min SDK", "${info.minSdk}")
                InfoRow("UID", "${info.uid}")
                InfoRow("Install Time", formatDate(info.installTime))
                InfoRow("Update Time", formatDate(info.updateTime))
            }
        }
        item {
            InfoCard("Paths") {
                InfoRow("Source Dir", info.sourceDir)
                InfoRow("Data Dir", info.dataDir)
                if (info.splitNames.isNotEmpty()) {
                    InfoRow("Splits", info.splitNames.joinToString(", "))
                }
            }
        }
        item {
            InfoCard("Signatures") {
                info.signatures.forEachIndexed { index, sig ->
                    InfoRow("SHA-256 ($index)", sig)
                }
            }
        }
    }
}

@Composable
fun AnalysisTab(info: AppDetailInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InfoCard("Tech Stack") {
                if (info.techStack.isEmpty()) {
                    Text("No specific framework detected")
                } else {
                    info.techStack.forEach { tech ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tech) }
                        )
                    }
                }
            }
        }
        item {
            InfoCard("Native Libraries") {
                if (info.nativeLibraries.isEmpty()) {
                    Text("No native libraries found")
                } else {
                    info.nativeLibraries.forEach { lib ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(lib.name, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                            Text("${lib.arch} • ${formatSize(lib.size)}", style = MaterialTheme.typography.bodySmall)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentsTab(info: AppDetailInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ComponentSection("Activities", info.activities) }
        item { ComponentSection("Services", info.services) }
        item { ComponentSection("Receivers", info.receivers) }
        item { ComponentSection("Providers", info.providers) }
    }
}

@Composable
fun ComponentSection(title: String, components: List<ComponentInfo>) {
    if (components.isNotEmpty()) {
        InfoCard("$title (${components.size})") {
            components.forEach { comp ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(comp.name, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                    }
                    if (comp.isExported) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("EXP", color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun PermissionsTab(info: AppDetailInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(info.permissions) { perm ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(perm.name, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                        Text("Level: ${perm.protectionLevel}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (perm.isGranted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Default.Cancel, contentDescription = "Denied", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickableWithClipboard(context, value)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

fun Modifier.clickableWithClipboard(context: Context, text: String): Modifier {
    return this.then(Modifier.clickable {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    })
}

// Helpers
private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

private fun formatSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb > 1) "%.2f MB".format(mb) else "%.2f KB".format(kb)
}
