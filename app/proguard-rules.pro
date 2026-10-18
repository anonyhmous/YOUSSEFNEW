# DriveFlow Driver - R8 / Proguard Hardening & Keep Rules

# 1. kotlinx.serialization rules
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,allowobfuscation,allowshrinking class * {
    <init>(...);
}

# Keep serializable domain models
-keep class com.driveflow.driver.core.model.** { *; }
-keep class com.driveflow.driver.data.prefs.** { *; }

# 2. ML Kit and OCR keep rules
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# 3. Strip all verbose and debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 4. Preserve line numbers and source files for crash reports if needed
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
