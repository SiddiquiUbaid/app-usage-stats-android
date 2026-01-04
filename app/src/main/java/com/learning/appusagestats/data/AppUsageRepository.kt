package com.learning.appusagestats.data

import android.graphics.drawable.Drawable

/**
 * Data class representing usage information for a single application.
 *
 * @property packageName The unique package identifier (e.g., "com.android.chrome")
 * @property appName Human-readable application name (e.g., "Chrome")
 * @property timeInForeground Total time the app was in foreground in milliseconds
 * @property icon Application icon drawable, null if unavailable
 */
data class AppUsageInfo(
        val packageName: String,
        val appName: String,
        val timeInForeground: Long,
        val icon: Drawable?
)

/** Interface for fetching and processing application usage statistics. */
interface AppUsageRepository {
        /**
         * Retrieves usage statistics for all user-facing apps used today.
         *
         * @return List of [AppUsageInfo] sorted by usage time (most used first)
         */
        fun getUsageStats(): List<AppUsageInfo>

        /**
         * Checks if the app has been granted PACKAGE_USAGE_STATS permission.
         *
         * @return true if permission is granted, false otherwise
         */
        fun hasUsagePermission(): Boolean
}
