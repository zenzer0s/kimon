# R8 Max Level Optimization Rules for Kimon

# Optimize code aggressively
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
-overloadaggressively

# Strip log calls in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <init>(...);
}

# DataStore Preferences
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    *;
}

# Compose Rules
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }

# Material Kolor
-dontwarn com.materialkolor.**
-keep class com.materialkolor.** { *; }
