package com.learning.appusagestats.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `formatTime returns 0m for small milliseconds`() {
        val result = TimeUtils.formatTime(500) // 0.5s
        assertEquals("0m", result)
    }

    @Test
    fun `formatTime returns minutes correctly`() {
        val result = TimeUtils.formatTime(120_000) // 2 minutes
        assertEquals("2m", result)
    }

    @Test
    fun `formatTime returns hours and minutes correctly`() {
        val result = TimeUtils.formatTime(3_661_000) // 1h 1m 1s.  3600s + 60s + 1s
        // 3661 / 1000 = 3661s
        // 3661 / 60 = 61m
        // 61 / 60 = 1h
        // 61 % 60 = 1m
        assertEquals("1h 1m", result)
    }

    @Test
    fun `formatTime handles exact hours`() {
        val result = TimeUtils.formatTime(3_600_000) // 1 hour
        assertEquals("1h 0m", result)
    }
}
