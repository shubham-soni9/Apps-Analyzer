package com.soni.appsanalyzer.data.util

import com.soni.appsanalyzer.domain.model.AppType
import java.io.File
import java.util.zip.ZipFile

object AppAnalyzer {

    fun analyzeApp(sourceDir: String): AppType {
        val file = File(sourceDir)
        if (!file.exists() || !file.canRead()) return AppType.UNKNOWN

        return try {
            ZipFile(file).use { zip ->
                val entries = zip.entries()
                var hasFlutter = false
                var hasReactNative = false
                var hasExpo = false

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name

                    if (name.contains("libflutter.so")) {
                        hasFlutter = true
                        // Flutter is distinct enough to return early? 
                        // Maybe check for other things just in case, but usually sufficient.
                        // Let's finish scanning to be safe or break if confident.
                    }
                    if (name.contains("libreactnativejni.so") || name.contains("index.android.bundle")) {
                        hasReactNative = true
                    }
                    if (name.contains("libexpo_modules_core.so")) {
                        hasExpo = true
                    }
                }

                when {
                    hasFlutter -> AppType.FLUTTER
                    hasExpo -> AppType.REACT_NATIVE_EXPO // Expo implies RN usually, but if found, it's Expo.
                    hasReactNative -> AppType.REACT_NATIVE
                    else -> AppType.NATIVE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppType.UNKNOWN
        }
    }
}
