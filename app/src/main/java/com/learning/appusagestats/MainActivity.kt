package com.learning.appusagestats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learning.appusagestats.data.AppUsageRepositoryImpl
import com.learning.appusagestats.ui.UsageListScreen
import com.learning.appusagestats.ui.theme.AppUsageStatsTheme

/**
 * Main entry point for the App Usage Stats application.
 *
 * This activity:
 * - Sets up the Compose UI with Material3 theme
 * - Initializes the MainViewModel
 * - Observes usage data and permission state
 * - Refreshes data when returning to the app (onResume)
 *
 * Note: Currently uses ViewModelProvider for ViewModel initialization. TODO: Migrate to 'by
 * viewModels()' delegate for cleaner code
 */
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manual Dependency Injection
        val factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                            val repository = AppUsageRepositoryImpl(applicationContext)
                            @Suppress("UNCHECKED_CAST")
                            return MainViewModel(application, repository) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                }
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            AppUsageStatsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Collect state from ViewModel
                    val usageList by viewModel.usageList.collectAsState()
                    val hasPermission by viewModel.hasPermission.collectAsState()

                    Box(modifier = Modifier.padding(innerPadding)) {
                        UsageListScreen(
                                usageList = usageList,
                                hasPermission = hasPermission,
                                onGrantPermissionClick = { viewModel.openUsageSettings() }
                        )
                    }
                }
            }
        }
    }

    /**
     * Refreshes permission status and usage data when activity resumes.
     *
     * This is crucial for detecting when user returns from Settings after granting
     * PACKAGE_USAGE_STATS permission.
     */
    override fun onResume() {
        super.onResume()
        viewModel.checkPermissionAndFetchData()
    }
}
