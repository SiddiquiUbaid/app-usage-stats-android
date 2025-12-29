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
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Last 24 hours
        val startTime = calendar.timeInMillis

        val usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                )

        val packageManager = context.packageManager

        return usageStatsList
                .filter { it.totalTimeInForeground > 0 }
                .mapNotNull { usageStats ->
                    try {
                        val appName =
                                packageManager
                                        .getApplicationLabel(
                                                packageManager.getApplicationInfo(
                                                        usageStats.packageName,
                                                        0
                                                )
                                        )
                                        .toString()
                        val icon = packageManager.getApplicationIcon(usageStats.packageName)

                        AppUsageInfo(
                                packageName = usageStats.packageName,
                                appName = appName,
                                timeInForeground = usageStats.totalTimeInForeground,
                                icon = icon
                        )
                    } catch (e: PackageManager.NameNotFoundException) {
                        null // Skip system apps/processes without launchable UI usually
                    }
                }
                .sortedByDescending { it.timeInForeground }
    }

    fun hasUsagePermission(): Boolean {
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
