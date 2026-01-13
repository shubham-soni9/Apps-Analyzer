package com.soni.appsanalyzer.domain.model

data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val appType: AppType
)
