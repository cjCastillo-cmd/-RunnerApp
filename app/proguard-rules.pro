# Runner App ProGuard Rules

# Mantener info de linea para stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Modelos de datos del proyecto (Gson necesita los campos)
-keep class com.gymnasioforce.runnerapp.network.** { *; }
-keep class com.gymnasioforce.runnerapp.data.local.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Google Play Services / Maps
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**