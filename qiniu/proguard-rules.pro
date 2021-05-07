# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/jerikc/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#mmfile
-keep class com.cosmos.mmfile.**{*;}
# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
native <methods>;
}
-keep class com.google.gson.** {*;}
-keep class com.cosmos.mdlog.** {*;}
-keep class com.momocv.** {*;}
-keep class com.core.glcore.util.** {*;}
-keep class com.imomo.momo.mediaencoder.** {*;}
-keep class com.imomo.momo.mediaencoder.MediaEncoder{*;}
-keep class com.cosmos.beauty.model.LandMarksEntity* {*;}
-keep public class com.immomo.mmdns.**{*;}
-keep class com.imomo.momo.mediamuxer.** {*;}
-keep class com.immomo.moment.mediautils.VideoDataRetrieverBySoft {*;}
-keep class com.immomo.moment.mediautils.YuvEditor {*;}
-keep class com.immomo.moment.mediautils.AudioMixerNative {*;}
-keep class com.immomo.moment.mediautils.MP4Fast {*;}
-keep class com.immomo.moment.mediautils.AudioResampleUtils {*;}
-keep class com.immomo.moment.mediautils.AudioSpeedControlPlayer {*;}
-keep interface com.immomo.moment.mediautils.AudioSpeedControlPlayer$* {*;}
-keep interface com.immomo.moment.mediautils.VideoDataRetrieverBySoft$* {*;}
-keep class com.immomo.moment.mediautils.VideoDataRetrieverBySoft$* {*;}
-keep class * extends com.immomo.moment.mediautils.MediaUtils {*;}
-keep class com.immomo.moment.mediautils.FFVideoDecoder* {*;}
-keep class com.momo.xeengine.audio.AudioEngine* {*;}
-keep class com.immomo.doki.media.entity.** {*;}
-keep class com.momo.mcamera.** {*;}
-dontwarn com.momo.mcamera.mask.**
-keep class com.google.gson.** {*;}
-keep class com.qiniu.pili.droid.streaming.** { *; }