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

#mmfile
-keep class com.mm.mmfile.core.**{*;}
# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
native <methods>;
}
-keep class com.google.gson.** {*;}
-keep class com.cosmos.mdlog.** {*;}
-keep class com.momocv.** {*;}
-keep class com.core.glcore.util.** {*;}
-keep public class com.immomo.mmdns.**{*;}
-keep class com.immomo.doki.media.entity.** {*;}
-keep class com.momo.mcamera.** {*;}
-dontwarn com.momo.mcamera.mask.**