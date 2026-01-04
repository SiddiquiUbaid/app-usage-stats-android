
package com.learning.appusagestats.util

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

object ColorUtils {

    // A predefined vibrant palette for better aesthetics
    private val VIBRANT_PALETTE = listOf(
        Color(0xFF6200EE), // Purple 500
        Color(0xFF03DAC5), // Teal 200
        Color(0xFFFF5722), // Deep Orange 500
        Color(0xFFFFC107), // Amber 500
        Color(0xFF2196F3), // Blue 500
        Color(0xFF4CAF50), // Green 500
        Color(0xFFE91E63), // Pink 500
        Color(0xFF9C27B0), // Purple 500
        Color(0xFF00BCD4), // Cyan 500
        Color(0xFFFF9800)  // Orange 500
    )

    /**
     * Returns a deterministic color for a given seed string (e.g., package name).
     */
    fun getAppColor(seed: String): Color {
        val hash = seed.hashCode().absoluteValue
        return VIBRANT_PALETTE[hash % VIBRANT_PALETTE.size]
    }
}
