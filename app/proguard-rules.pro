# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Hilt
-keep class * extends androidx.lifecycle.ViewModel
-keep class dagger.hilt.android.internal.** { *; }
-keep class hilt_aggregated_deps.** { *; }

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory { 
    public static final kotlinx.coroutines.MainCoroutineDispatcher a;
}

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.Gson
-keep class com.google.gson.GsonBuilder
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.annotations.** { *; }

# iText7
-keep class com.itextpdf.** { *; }
-keep interface com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep data models (adjust package name as needed)
-keep class com.example.shuigongrizhi.data.model.** { *; }