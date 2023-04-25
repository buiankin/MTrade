# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
#-keep public class com.sails.engine.patterns.IconPatterns
-dontwarn org.apache.**
-keep class org.kobjects.** {*;}
-keep class org.ksoap2.** {*;}
-dontwarn org.kxml2.**
-keep class org.kxml2.** { *;}
-dontwarn org.xmlpull.v1.**
-keep class org.xmlpull.v1.** {*;}
-dontwarn org.conscrypt.*




# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
#-keepclassmembers public class * extends android.view.View {
#   void set*(***);
#   *** get*();
#}

# We want to keep methods in Activity that could be used in the XML attribute onClick
#-keepclassmembers class * extends android.app.Activity {
#   public void *(android.view.View);
#}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-dontwarn javax.xml.stream.**

-keepattributes *Annotation*

-dontwarn net.sourceforge.jheader.**

-dontwarn **CompatHoneycomb
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep class androidx.** { *; }



-keep public class org.apache.commons.** { *; }
-keep class net.sourceforge.jheader.** { *; }
-keep class ru.code22.mtrade.MyDatabase$OrderRecord { *; }

-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-optimizationpasses 1

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

