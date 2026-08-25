# Room Database keep rules
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.example.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}

# SQLCipher keep rules
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Security / EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Moshi & Retrofit
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }
-keepattributes EnclosingMethod,InnerClasses,Signature
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Preserve line numbers and attributes for local stack traces & reflection
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Keep Compose metadata
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Ignore optional/runtime dependencies for OkHttp and Google Crypto Tink
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

