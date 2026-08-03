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

# Preserve line numbers and attributes for local stack traces & reflection
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Keep Compose metadata
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
