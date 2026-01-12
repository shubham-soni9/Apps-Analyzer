package com.soni.appsanalyzer.data.mapper

import android.content.Context
import com.soni.appsanalyzer.data.local.AppEntity
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.model.AppType

fun AppEntity.toAppInfo(context: Context): AppInfo {
    return AppInfo(
        name = name,
        packageName = packageName,
        versionName = versionName,
        appType =
            try {
                AppType.valueOf(appType)
            } catch (e: Exception) {
                AppType.UNKNOWN
            },
        versionCode = versionCode
    )
}

fun AppInfo.toAppEntity(): AppEntity {
    return AppEntity(
            packageName = packageName,
            name = name,
            versionName = versionName,
            versionCode = versionCode,
            appType = appType.name
    )
}
