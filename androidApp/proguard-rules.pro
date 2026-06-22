# ------------------------------------------------------------------------------
# Kotlin & General Multiplatform Rules
# ------------------------------------------------------------------------------

# Keep Kotlin reflection metadata
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# ------------------------------------------------------------------------------
# Kotlinx Serialization Rules
# ------------------------------------------------------------------------------

# Keep all classes annotated with @Serializable and their generated serializers
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** $$serializer;
}

# ------------------------------------------------------------------------------
# Voyager Navigator Rules
# ------------------------------------------------------------------------------

# Voyager instantiates screens and viewmodels dynamically. Keep them to avoid crashes.
-dontwarn cafe.adriel.voyager.**
-keep class * implements cafe.adriel.voyager.core.screen.Screen { *; }
-keep class * extends cafe.adriel.voyager.core.model.ScreenModel { *; }

# ------------------------------------------------------------------------------
# Ktor & OkHttp Rules
# ------------------------------------------------------------------------------

# Ktor HTTP client rules
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# OkHttp engine rules (used by Ktor Android engine)
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# ------------------------------------------------------------------------------
# Koin Dependency Injection Rules
# ------------------------------------------------------------------------------

# Keep Koin classes and ignore common warnings from internal reflection
-keep class io.insertkoin.** { *; }
-dontwarn io.insertkoin.**

# ------------------------------------------------------------------------------
# SQLDelight Database Rules
# ------------------------------------------------------------------------------

# Keep generated SQLDelight database & queries
-keep class org.bibletranslationtools.database.** { *; }
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**

# ------------------------------------------------------------------------------
# Logging Frameworks (SLF4J / Logback)
# ------------------------------------------------------------------------------

-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**
-dontwarn io.github.oshai.kotlinlogging.**
