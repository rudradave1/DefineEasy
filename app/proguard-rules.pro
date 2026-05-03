# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Retrofit
-keepattributes Signature, EnclosingMethod, InnerClasses, *Annotation*
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Gson
-keepattributes Signature, EnclosingMethod, InnerClasses, *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class * extends com.google.gson.TypeAdapter
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Feature Dictionary Models & DTOs
# These MUST be kept to prevent ClassCastException (LinkedTreeMap)
-keep class com.rudra.defineeasy.feature_dictionary.data.remote.dto.** { *; }
-keep class com.rudra.defineeasy.feature_dictionary.data.local.entity.** { *; }
-keep class com.rudra.defineeasy.feature_dictionary.domain.model.** { *; }
-keep class com.rudra.defineeasy.feature_dictionary.data.collection.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }

# SQLCipher / SQLite
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }
-keep class androidx.sqlite.** { *; }

# Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }

# Common Proguard attributes
-keepattributes SourceFile,LineNumberTable

# Optional OkHttp security providers
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn sun.misc.Unsafe
