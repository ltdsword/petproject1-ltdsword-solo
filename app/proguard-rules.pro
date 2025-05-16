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

# Keep Retrofit interfaces and method signatures
-keep interface * {
    @retrofit2.* <methods>;
}

# Keep Retrofit method annotations and response types
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations

# Keep Gson @SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep data classes and interfaces in your app's package
-keep class com.example.cuoi.** { *; }
-keep interface com.example.cuoi.** { *; }

# Optional but recommended for Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

