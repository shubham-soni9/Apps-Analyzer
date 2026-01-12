package com.soni.appsanalyzer.domain.model

data class AppDetailInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Any?, // Can be Drawable or Bitmap or Uri, but for now we'll handle loading in UI. Actually, better not to store UI objects here. We'll load by package name.
    val installTime: Long,
    val updateTime: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val uid: Int,
    val sourceDir: String,
    val dataDir: String,
    val splitNames: List<String>,
    val signatures: List<String>, // Fingerprints (MD5/SHA1/SHA256)
    val activities: List<ComponentInfo>,
    val services: List<ComponentInfo>,
    val receivers: List<ComponentInfo>,
    val providers: List<ComponentInfo>,
    val permissions: List<PermissionInfo>,
    val nativeLibraries: List<NativeLibInfo>,
    val techStack: List<String>
)

data class ComponentInfo(
    val name: String,
    val isExported: Boolean,
    val type: ComponentType
)

enum class ComponentType {
    ACTIVITY, SERVICE, RECEIVER, PROVIDER
}

data class PermissionInfo(
    val name: String,
    val isGranted: Boolean, // Note: We might not be able to check runtime status for other apps easily
    val protectionLevel: Int
)

data class NativeLibInfo(
    val name: String,
    val size: Long,
    val arch: String
)
