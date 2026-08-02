# Viking Security Agent ProGuard Rules

# Keep generated BuildConfig class
-keep class com.apocalyptolabs.viking.BuildConfig { *; }
-keepclassmembers class com.apocalyptolabs.viking.BuildConfig { *; }
-keep class **.BuildConfig { *; }
-keepclassmembers class **.BuildConfig { *; }

# Keep MediaPipe Task GenAI LLM classes
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# Keep Hilt & Dagger Generated Classes
-keep class dagger.hilt.** { *; }
-keep class **.*_HiltModules** { *; }
-keep class **.*_Factory { *; }
-keep class **.*_MembersInjector { *; }

# Keep Domain Models used in AI inference & JSON parsing
-keep class com.apocalyptolabs.viking.core.model.** { *; }
-keepclassmembers class com.apocalyptolabs.viking.core.model.** { *; }

# Keep Room DB Entities, DAOs, and Database
-keep class com.apocalyptolabs.viking.data.db.** { *; }
-keepclassmembers class com.apocalyptolabs.viking.data.db.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep SQLCipher Native Methods
-keep class net.zetetic.database.sqlcipher.** { *; }
-keepclassmembers class net.zetetic.database.sqlcipher.** { *; }
-keep class net.sqlcipher.** { *; }

# Suppress serialization and annotation processor warnings
-dontwarn kotlinx.serialization.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.processing.**
-dontwarn javax.lang.model.**
-dontwarn com.google.auto.value.**
-dontwarn autovalue.shaded.**
