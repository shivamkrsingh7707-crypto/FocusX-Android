# Keep Compose runtime helpers
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# JankStats
-dontwarn androidx.metrics.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
