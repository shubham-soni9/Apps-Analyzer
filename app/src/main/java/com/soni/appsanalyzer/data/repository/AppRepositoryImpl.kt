package com.soni.appsanalyzer.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import com.soni.appsanalyzer.data.local.AppDao
import com.soni.appsanalyzer.data.mapper.toAppEntity
import com.soni.appsanalyzer.data.mapper.toAppInfo
import com.soni.appsanalyzer.data.util.AppAnalyzer
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepositoryImpl
@Inject
constructor(@ApplicationContext private val context: Context, private val appDao: AppDao) :
        AppRepository {

    override fun getInstalledApps(): Flow<List<AppInfo>> {
        return appDao.getAllApps().map { entities -> entities.map { it.toAppInfo(context) } }
    }

    override suspend fun syncApps() {
        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(0)

            val appInfos =
                    packages
                            .filter { packageInfo ->
                                val appInfo = packageInfo.applicationInfo
                                appInfo != null &&
                                        (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                            }
                            .map { packageInfo ->
                                async {
                                    val appInfo = packageInfo.applicationInfo!!
                                    AppInfo(
                                            name = appInfo.loadLabel(packageManager).toString(),
                                            packageName = packageInfo.packageName,
                                            versionName = packageInfo.versionName ?: "Unknown",
                                            versionCode =
                                                    if (android.os.Build.VERSION.SDK_INT >=
                                                                    android.os.Build.VERSION_CODES.P
                                                    ) {
                                                        packageInfo.longVersionCode
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        packageInfo.versionCode.toLong()
                                                    },
                                            appType = AppAnalyzer.analyzeApp(appInfo.sourceDir)
                                    )
                                }
                            }
                            .awaitAll()

            val entities = appInfos.map { it.toAppEntity() }
            appDao.replaceApps(entities)
        }
    }
}
