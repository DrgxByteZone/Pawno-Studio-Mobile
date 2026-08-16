# Proguard rules for Pawno Studio Mobile

# Keep native JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Sora Editor
-keep class io.github.rosemoe.sora.** { *; }

# Keep Room
-keep class androidx.room.** { *; }
