# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepattributes *Annotation*
#-keepclassmembers class * {
#    @org.greenrobot.eventbus.Subscribe <methods>;
#}
#-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#
## Only required if you use AsyncExecutor
#-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
#    <init>(java.lang.Throwable);
#}
#-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
#-dontwarn com.facebook.infer.annotation.Nullsafe
## Uncomment this to preserve the line number information for
## debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
#-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
#-dontwarn com.facebook.infer.annotation.Nullsafe
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keep class com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.**{ *; }
# Keep all classes in the android.view package
#-keep class android.view.** { *; }
#
##-keepclassmembers class com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo { *; }
#
## Keep all classes that implement ViewGroup.MarginLayoutParams
#-keep class * implements android.view.ViewGroup.MarginLayoutParams { *; }
#
#-keepattributes Signature
#
## Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
# -keep class com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo.** { *; }