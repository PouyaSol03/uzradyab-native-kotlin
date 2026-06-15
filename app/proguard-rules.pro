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

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.example.uzradyab.data.remote.dto.** { *; }
-keep class com.example.uzradyab.domain.model.** { *; }

# Retrofit
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# ==========================================
# Room Database Rules
# ==========================================
# حفظ موجودیت‌های دیتابیس برای جلوگیری از کرش کردن Room در نسخه نهایی
-keep class com.example.uzradyab.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase

# ==========================================
# Security & Logging Rules
# ==========================================
# حذف کامل لاگ‌های سیستمی از نسخه Release (الزام امنیتی گوگل پلی)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# حذف لاگر اختصاصی اپلیکیشن برای جلوگیری از نشت اطلاعات حساس
-assumenosideeffects class com.example.uzradyab.core.debug.AppLogger {
    public static *** *(...);
}