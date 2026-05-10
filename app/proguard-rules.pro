# Default ProGuard rules.
# Keep Room entities and OSMDroid reflection-friendly classes.
-keep class androidx.room.** { *; }
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
