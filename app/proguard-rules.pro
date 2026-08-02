# ProGuard & R8 optimization rules for Code Studio (VS Code Android)

# Keep line number info for crash reports & stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Jetpack Compose & Material 3
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Room Database Entities & DAOs
-keep class com.example.data.local.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# Keep Data Models & Enums
-keep class com.example.model.** { *; }
-keepclassmembers enum com.example.model.** { *; }

# Keep Moshi JSON Codegen & Adapter classes
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

# Keep Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
