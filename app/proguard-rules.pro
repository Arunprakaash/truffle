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

# Stack traces (Play / crash reports): keep line numbers, hide real source paths.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room ships consumer rules; keep the database impl if R8 gets aggressive.
-keep class com.truffleapp.truffle.data.db.LedgerDatabase_Impl { *; }

# WorkManager lists worker implementations by name.
-keep class com.truffleapp.truffle.reminders.BillReminderWorker { *; }