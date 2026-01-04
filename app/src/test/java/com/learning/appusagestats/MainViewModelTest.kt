package com.learning.appusagestats

import android.app.Application
import com.learning.appusagestats.data.AppUsageInfo
import com.learning.appusagestats.data.AppUsageRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val application = mockk<Application>(relaxed = true)
    private val repository = mockk<AppUsageRepository>()

    @Test
    fun `initial state is empty list and no permission`() {
        val viewModel = MainViewModel(application, repository, UnconfinedTestDispatcher())

        assertEquals(emptyList<AppUsageInfo>(), viewModel.usageList.value)
        assertEquals(false, viewModel.hasPermission.value)
    }

    @Test
    fun `checkPermissionAndFetchData updates permission state only when denied`() {
        // Given
        every { repository.hasUsagePermission() } returns false
        val viewModel = MainViewModel(application, repository, UnconfinedTestDispatcher())

        // When
        viewModel.checkPermissionAndFetchData()

        // Then
        assertEquals(false, viewModel.hasPermission.value)
        assertEquals(emptyList<AppUsageInfo>(), viewModel.usageList.value)
    }

    @Test
    fun `checkPermissionAndFetchData fetches data when permission granted`() = runTest {
        // Given
        val mockData = listOf(AppUsageInfo("com.test", "Test App", 1000L, null))
        every { repository.hasUsagePermission() } returns true
        coEvery { repository.getUsageStats() } returns mockData

        val viewModel = MainViewModel(application, repository, UnconfinedTestDispatcher())

        // When
        viewModel.checkPermissionAndFetchData()

        // Then
        assertEquals(true, viewModel.hasPermission.value)
        // Since we verify StateFlow value matching, we depend on UnconfinedTestDispatcher
        // to execute coroutines immediately.
        assertEquals(mockData, viewModel.usageList.value)
    }
}
