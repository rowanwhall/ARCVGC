# ===== Kotlin Serialization =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.arcvgc.app.**$$serializer { *; }
-keepclassmembers class com.arcvgc.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.arcvgc.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===== Ktor / OkHttp =====
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# ===== Hilt / Dagger =====
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ===== Coil =====
-keep class coil3.** { *; }

# ===== Sentry =====
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# ===== General Kotlin =====
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
