package com.learning.appusagestats.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.learning.appusagestats.data.AppUsageInfo
import org.junit.Rule
import org.junit.Test

class UsageListScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun permissionRequired_isDisplayed_whenPermissionDenied() {
        composeTestRule.setContent {
            UsageListScreen(
                    usageList = emptyList(),
                    hasPermission = false,
                    onGrantPermissionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Usage Permission Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grant Permission").assertIsDisplayed()
    }

    @Test
    fun noDataMessage_isDisplayed_whenPermissionGranted_andListEmpty() {
        composeTestRule.setContent {
            UsageListScreen(
                    usageList = emptyList(),
                    hasPermission = true,
                    onGrantPermissionClick = {}
            )
        }

        composeTestRule.onNodeWithText("No usage data found for today.").assertIsDisplayed()
    }

    @Test
    fun appItem_isDisplayed_whenDataAvailable() {
        val testApp =
                AppUsageInfo(
                        packageName = "com.example.app",
                        appName = "Test App",
                        timeInForeground = 120_000L, // 2 minutes
                        icon = null
                )

        composeTestRule.setContent {
            UsageListScreen(
                    usageList = listOf(testApp),
                    hasPermission = true,
                    onGrantPermissionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
        composeTestRule.onNodeWithText("2m").assertIsDisplayed()
    }
}
