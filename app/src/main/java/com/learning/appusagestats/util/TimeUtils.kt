package com.learning.appusagestats.util

/** Utility functions for time formatting. */
object TimeUtils {
    /**
     * Formats milliseconds into human-readable time string.
     *
     * Examples:
     * - 3661000ms -> "1h 1m"
     * - 120000ms -> "2m"
     * - 3600000ms -> "1h 0m"
     * - 45000ms -> "0m"
     *
     * @param millis Time in milliseconds
     * @return Formatted string (e.g., "2h 15m" or "45m")
     */
    fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return if (hours > 0) {
            "${hours}h ${minutes % 60}m"
        } else {
            "${minutes}m"
        }
    }
}
