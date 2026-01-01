package com.learning.appusagestats.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import java.util.Calendar

data class AppUsageInfo(
        val packageName: String,
        val appName: String,
        val timeInForeground: Long,
        val icon: Drawable?
)

class AppUsageRepository(private val context: Context) {

        fun getUsageStats(): List<AppUsageInfo> {
                val usageStatsManager =
                        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis

                // Reset to midnight for "Today's" stats
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startTime = calendar.timeInMillis

                val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
                val event = android.app.usage.UsageEvents.Event()

                val foregroundTimes = mutableMapOf<String, Long>()
                val lastResumeTime = mutableMapOf<String, Long>()

                while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event)
                        val packageName = event.packageName ?: continue

                        when (event.eventType) {
                                android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED -> {
                                        lastResumeTime[packageName] = event.timeStamp
                                }
                                android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED -> {
                                        lastResumeTime[packageName]?.let { start ->
                                                val duration = event.timeStamp - start
                                                if (duration > 0) {
                                                        foregroundTimes[packageName] =
                                                                (foregroundTimes[packageName]
                                                                        ?: 0) + duration
                                                }
                                                // Don't remove here for accuracy in some edge cases
                                                // but
                                                // usually safe.
                                                // Sticking to basic logic:
                                                lastResumeTime.remove(packageName)
                                        }
                                }
                        }
                }

                // Add pending time for currently active apps (Resumed but not Paused)
                lastResumeTime.forEach { (packageName, start) ->
                        val duration = endTime - start
                        if (duration > 0) {
                                foregroundTimes[packageName] =
                                        (foregroundTimes[packageName] ?: 0) + duration
                        }
                }

                val packageManager = context.packageManager

                return foregroundTimes
                        .entries
                        .filter { (packageName, totalTime) ->
                                totalTime > 0 &&
                                        packageManager.getLaunchIntentForPackage(packageName) !=
                                                null
                        }
                        .mapNotNull { (packageName, totalTime) ->
                                try {
                                        val appName =
                                                packageManager
                                                        .getApplicationLabel(
                                                                packageManager.getApplicationInfo(
                                                                        packageName,
                                                                        0
                                                                )
                                                        )
                                                        .toString()
                                        val icon = packageManager.getApplicationIcon(packageName)

                                        AppUsageInfo(
                                                packageName = packageName,
                                                appName = appName,
                                                timeInForeground = totalTime,
                                                icon = icon
                                        )
                                } catch (e: PackageManager.NameNotFoundException) {
                                        null
                                }
                        }
                        .sortedByDescending { it.timeInForeground }
        }

        fun hasUsagePermission(): Boolean {
                val appOps =
                        context.getSystemService(Context.APP_OPS_SERVICE) as
                                android.app.AppOpsManager
                val mode =
                        appOps.unsafeCheckOpNoThrow(
                                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(),
                                context.packageName
                        )
                return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
}
