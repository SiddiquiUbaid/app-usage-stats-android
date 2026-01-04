package com.learning.appusagestats.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.util.Calendar

/** Implementation of [AppUsageRepository] using Android's UsageEvents API. */
class AppUsageRepositoryImpl(private val context: Context) : AppUsageRepository {

    override fun getUsageStats(): List<AppUsageInfo> {
        val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        // Reset calendar to midnight (00:00:00.000) for "today's" stats
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // Query all usage events from midnight to now
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        // Track accumulated foreground time per package
        val foregroundTimes = mutableMapOf<String, Long>()
        // Track when each app was last brought to foreground
        val lastResumeTime = mutableMapOf<String, Long>()

        // Process all events chronologically
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName ?: continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    // App came to foreground - record the timestamp
                    lastResumeTime[packageName] = event.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    // App went to background - calculate session duration
                    lastResumeTime[packageName]?.let { start ->
                        val duration = event.timeStamp - start
                        if (duration > 0) {
                            // Add this session's duration to total foreground time
                            foregroundTimes[packageName] =
                                    (foregroundTimes[packageName] ?: 0) + duration
                        }
                        // Clear the resume timestamp since session ended
                        lastResumeTime.remove(packageName)
                    }
                }
            }
        }

        // Handle apps still in foreground (resumed but not yet paused)
        // Calculate their usage from resume time to now
        lastResumeTime.forEach { (packageName, start) ->
            val duration = endTime - start
            if (duration > 0) {
                foregroundTimes[packageName] = (foregroundTimes[packageName] ?: 0) + duration
            }
        }

        val packageManager = context.packageManager

        return foregroundTimes
                .entries
                .filter { (packageName, totalTime) ->
                    // Only include apps with:
                    // 1. Non-zero usage time
                    // 2. A launch intent (filters out background services/system processes)
                    totalTime > 0 && packageManager.getLaunchIntentForPackage(packageName) != null
                }
                .mapNotNull { (packageName, totalTime) ->
                    try {
                        // Fetch app metadata from PackageManager
                        val appName =
                                packageManager
                                        .getApplicationLabel(
                                                packageManager.getApplicationInfo(packageName, 0)
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
                        // App was uninstalled after usage event - skip it
                        null
                    }
                }
                .sortedByDescending { it.timeInForeground }
    }

    override fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode =
                appOps.unsafeCheckOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.packageName
                )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}
