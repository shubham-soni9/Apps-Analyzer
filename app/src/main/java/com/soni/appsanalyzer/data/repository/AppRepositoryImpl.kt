package com.soni.appsanalyzer.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.soni.appsanalyzer.data.local.AppDao
import com.soni.appsanalyzer.data.mapper.toAppEntity
import com.soni.appsanalyzer.data.mapper.toAppInfo
import com.soni.appsanalyzer.data.util.AppAnalyzer
import com.soni.appsanalyzer.domain.model.AppDetailInfo
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.model.ComponentInfo
import com.soni.appsanalyzer.domain.model.ComponentType
import com.soni.appsanalyzer.domain.model.NativeLibInfo
import com.soni.appsanalyzer.domain.model.PermissionInfo
import com.soni.appsanalyzer.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
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

    override suspend fun getAppDetails(packageName: String): AppDetailInfo {
        return withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val flags =
                    PackageManager.GET_ACTIVITIES or
                            PackageManager.GET_SERVICES or
                            PackageManager.GET_RECEIVERS or
                            PackageManager.GET_PROVIDERS or
                            PackageManager.GET_PERMISSIONS or
                            PackageManager.GET_META_DATA or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                PackageManager.GET_SIGNING_CERTIFICATES
                            } else {
                                @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
                            }

            val packageInfo = pm.getPackageInfo(packageName, flags)
            val appInfo = packageInfo.applicationInfo!!

            // Signatures
            val signatures = getSignatures(packageInfo)

            // Components
            val activities =
                    packageInfo.activities?.map {
                        ComponentInfo(it.name, it.exported, ComponentType.ACTIVITY)
                    }
                            ?: emptyList()

            val services =
                    packageInfo.services?.map {
                        ComponentInfo(it.name, it.exported, ComponentType.SERVICE)
                    }
                            ?: emptyList()

            val receivers =
                    packageInfo.receivers?.map {
                        ComponentInfo(it.name, it.exported, ComponentType.RECEIVER)
                    }
                            ?: emptyList()

            val providers =
                    packageInfo.providers?.map {
                        ComponentInfo(it.name, it.exported, ComponentType.PROVIDER)
                    }
                            ?: emptyList()

            // Permissions
            val reqPermissions = packageInfo.requestedPermissions
            val permissions =
                    reqPermissions?.indices?.map { i ->
                        val name = reqPermissions[i]
                        val flags = packageInfo.requestedPermissionsFlags?.get(i) ?: 0
                        val isGranted = (flags and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0

                        var pLevel = 0
                        try {
                            val pInfo = pm.getPermissionInfo(name, 0)
                            pLevel = pInfo.protectionLevel
                        } catch (e: Exception) {}

                        PermissionInfo(name, isGranted, pLevel)
                    }
                            ?: emptyList()

            // Analyze APK
            val analysis = analyzeApk(appInfo.sourceDir)

            val versionCode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
                    }

            AppDetailInfo(
                    name = appInfo.loadLabel(pm).toString(),
                    packageName = packageName,
                    versionName = packageInfo.versionName ?: "Unknown",
                    versionCode = versionCode,
                    icon = null,
                    installTime = packageInfo.firstInstallTime,
                    updateTime = packageInfo.lastUpdateTime,
                    minSdk =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                    appInfo.minSdkVersion
                            else 0,
                    targetSdk = appInfo.targetSdkVersion,
                    uid = appInfo.uid,
                    sourceDir = appInfo.sourceDir,
                    dataDir = appInfo.dataDir,
                    splitNames =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    appInfo.splitNames?.toList() ?: emptyList()
                            else emptyList(),
                    signatures = signatures,
                    activities = activities,
                    services = services,
                    receivers = receivers,
                    providers = providers,
                    permissions = permissions,
                    nativeLibraries = analysis.second,
                    techStack = analysis.first
            )
        }
    }

    private fun getSignatures(packageInfo: PackageInfo): List<String> {
        val sigs =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
                } else {
                    @Suppress("DEPRECATION") packageInfo.signatures ?: emptyArray()
                }

        return sigs.map { signature -> hash(signature.toByteArray(), "SHA-256") }
    }

    private fun hash(data: ByteArray, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(data)
        return bytes.joinToString("") { "%02X".format(it) }
    }

    private fun analyzeApk(sourceDir: String): Pair<List<String>, List<NativeLibInfo>> {
        val techStack = mutableSetOf<String>()
        val nativeLibs = mutableListOf<NativeLibInfo>()

        try {
            ZipFile(File(sourceDir)).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name

                    if (name.startsWith("lib/")) {
                        // lib/arm64-v8a/libfoo.so
                        val parts = name.split("/")
                        if (parts.size >= 3) {
                            val arch = parts[1]
                            val libName = parts.last()
                            nativeLibs.add(NativeLibInfo(libName, entry.size, arch))
                        }
                    }

                    // Framework detection logic
                    if (name.contains("libflutter.so") || name.contains("flutter_assets/"))
                            techStack.add("Flutter")
                    if (name.contains("libreactnativejni.so") ||
                                    name.contains("index.android.bundle")
                    )
                            techStack.add("React Native")
                    if (name.contains("kotlin/")) techStack.add("Kotlin")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (techStack.isEmpty()) techStack.add("Native (Likely)")

        return Pair(techStack.toList(), nativeLibs)
    }
}
