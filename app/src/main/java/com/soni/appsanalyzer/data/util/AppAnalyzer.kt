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
                val entries = zip.entries().toList()
                val entryNames = entries.map { it.name.lowercase() }

                // Flutter detection patterns
                val flutterIndicators = listOf(
                    "libflutter.so",
                    "libapp.so",
                    "flutter_assets/",
                    "flutter_assets/kernel_blob.bin",
                    "flutter_assets/isolate_snapshot_data",
                    "flutter_assets/vm_snapshot_data",
                    "io.flutter.embedding",
                    "io/flutter/",
                    "classes.dex" // Check for Flutter engine classes in DEX
                )

                // React Native detection patterns
                val reactNativeIndicators = listOf(
                    "libreactnativejni.so",
                    "index.android.bundle",
                    "assets/index.android.bundle",
                    "libjscexecutor.so",
                    "libhermes.so",
                    "libjsi.so",
                    "libreactnative.so",
                    "libfbjni.so",
                    "com/facebook/react/",
                    "assets/index.bundle"
                )

                // Expo detection patterns
                val expoIndicators = listOf(
                    "libexpo_modules_core.so",
                    "expo-modules-core",
                    "host.exp.exponent",
                    "versioned.host.exp.exponent",
                    "expo/modules/"
                )

                // Check manifest for framework-specific entries
                val manifestEntry = entries.find {
                    it.name == "AndroidManifest.xml"
                }
                var manifestContent = ""
                if (manifestEntry != null) {
                    try {
                        val inputStream = zip.getInputStream(manifestEntry)
                        // Note: This reads binary XML, you might need proper XML parser
                        // For now, reading as string to catch package names
                        manifestContent = inputStream.bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        // Continue without manifest content
                    }
                }

                // Count matches for each framework
                var flutterScore = 0
                var reactNativeScore = 0
                var expoScore = 0

                // Check Flutter indicators
                for (indicator in flutterIndicators) {
                    if (entryNames.any { it.contains(indicator.lowercase()) }) {
                        flutterScore++
                    }
                }

                // Check React Native indicators
                for (indicator in reactNativeIndicators) {
                    if (entryNames.any { it.contains(indicator.lowercase()) }) {
                        reactNativeScore++
                    }
                }

                // Check Expo indicators
                for (indicator in expoIndicators) {
                    if (entryNames.any { it.contains(indicator.lowercase()) }) {
                        expoScore++
                    }
                }

                // Additional manifest checks
                if (manifestContent.isNotEmpty()) {
                    if (manifestContent.contains("io.flutter", ignoreCase = true)) {
                        flutterScore += 2
                    }
                    if (manifestContent.contains("com.facebook.react", ignoreCase = true)) {
                        reactNativeScore += 2
                    }
                    if (manifestContent.contains("expo", ignoreCase = true) ||
                        manifestContent.contains("host.exp.exponent", ignoreCase = true)) {
                        expoScore += 2
                    }
                }

                // Check lib folders for framework-specific native libraries
                val libPaths = entryNames.filter { it.startsWith("lib/") }
                val hasFlutterLibs = libPaths.any { it.contains("libflutter.so") }
                val hasRNLibs = libPaths.any {
                    it.contains("libreactnative") || it.contains("libhermes.so") || it.contains("libjsi.so")
                }
                val hasExpoLibs = libPaths.any { it.contains("libexpo") }

                if (hasFlutterLibs) flutterScore += 3
                if (hasRNLibs) reactNativeScore += 3
                if (hasExpoLibs) expoScore += 3

                // Check assets folder structure
                val assetPaths = entryNames.filter { it.startsWith("assets/") }
                if (assetPaths.any { it.contains("flutter_assets") }) {
                    flutterScore += 2
                }
                if (assetPaths.any { it.contains("index.android.bundle") || it.contains("index.bundle") }) {
                    reactNativeScore += 2
                }

                // Determine app type based on scores
                // Use threshold to avoid false positives
                val minThreshold = 2

                when {
                    flutterScore >= minThreshold && flutterScore > reactNativeScore && flutterScore > expoScore ->
                        AppType.FLUTTER
                    expoScore >= minThreshold && expoScore >= reactNativeScore ->
                        AppType.REACT_NATIVE_EXPO
                    reactNativeScore >= minThreshold ->
                        AppType.REACT_NATIVE
                    else -> AppType.NATIVE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppType.UNKNOWN
        }
    }
}