package com.soni.appsanalyzer.domain.model

enum class AppType(val displayName: String) {
    FLUTTER("Flutter"),
    REACT_NATIVE("React Native"),
    REACT_NATIVE_EXPO("React Native with Expo"),
    KMM("KMM"),
    NATIVE("Native Only"),
    UNKNOWN("Unknown")
}
