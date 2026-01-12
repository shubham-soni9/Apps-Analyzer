package com.soni.appsanalyzer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
        @PrimaryKey val packageName: String,
        val name: String,
        val versionName: String,
        val appType: String,
        val icon: ByteArray?
)
