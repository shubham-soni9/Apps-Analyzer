package com.soni.appsanalyzer.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val versionName: String
)
