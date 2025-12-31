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

                // Aggregate usage stats by package name
                val aggregatedStats =
                        usageStatsList.groupBy { it.packageName }.mapValues { entry ->
                                entry.value.sumOf { it.totalTimeInForeground }
                        }

                return aggregatedStats
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
