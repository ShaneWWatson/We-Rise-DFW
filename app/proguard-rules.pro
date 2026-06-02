# Default ProGuard rules.
# Keep Room entities and OSMDroid reflection-friendly classes.
-keep class androidx.room.** { *; }
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# General security: Remove logging code in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Preserve line numbers for stack traces in crash reports (if any added later)
-keepattributes SourceFile,LineNumberTable
