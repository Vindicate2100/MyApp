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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# iText7 rules
-keep class com.itextpdf.** { *; }
-keep class com.itextpdf.kernel.** { *; }
-keep class com.itextpdf.layout.** { *; }
-keep class com.itextpdf.io.** { *; }

# Jackson rules
-keep class com.fasterxml.jackson.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }
-keep class com.fasterxml.jackson.core.** { *; }
-keep class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.**
-dontwarn com.fasterxml.jackson.annotation.**
-dontwarn com.fasterxml.jackson.core.**
-dontwarn com.fasterxml.jackson.databind.**

# AWT rules
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.xml.crypto.**
-dontwarn org.apache.jcp.xml.dsig.**
-dontwarn org.apache.xml.security.**

# SLF4J rules
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep your model classes
-keep class com.example.myapplication.data.** { *; }
-keep class com.example.myapplication.services.** { *; }
-keep class com.example.myapplication.viewmodels.** { *; }