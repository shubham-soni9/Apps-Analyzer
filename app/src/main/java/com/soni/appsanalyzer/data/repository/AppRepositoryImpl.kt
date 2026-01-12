package com.soni.appsanalyzer.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepositoryImpl(private val context: Context) : AppRepository {

    override suspend fun getInstalledApps(): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            // Use GET_META_DATA or 0.
            val packages = packageManager.getInstalledPackages(0)

            packages.mapNotNull { packageInfo ->
                val appInfo = packageInfo.applicationInfo
                // Filter out system apps and null applicationInfo
                if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    AppInfo(
                        name = appInfo.loadLabel(packageManager).toString(),
                        packageName = packageInfo.packageName,
                        icon = appInfo.loadIcon(packageManager),
                        versionName = packageInfo.versionName ?: "Unknown"
                    )
                } else {
                    null
                }
            }
        }
    }
}
