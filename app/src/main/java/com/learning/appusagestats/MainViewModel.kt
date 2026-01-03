package com.learning.appusagestats

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learning.appusagestats.data.AppUsageInfo
import com.learning.appusagestats.data.AppUsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app usage data and permission state.
 *
 * This ViewModel:
 * - Checks if PACKAGE_USAGE_STATS permission is granted
 * - Fetches usage statistics from the repository
 * - Exposes UI state via StateFlow for Compose
 * - Handles navigation to system settings for permission grant
 *
 * Note: Currently uses direct repository instantiation. TODO: Introduce dependency injection
 * (Hilt/Koin) for better testability
 *
 * @property application Application context for accessing system services
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppUsageRepository(application)

    private val _usageList = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    /**
     * StateFlow emitting the list of app usage statistics. Empty list when permission is not
     * granted or no data available.
     */
    val usageList: StateFlow<List<AppUsageInfo>> = _usageList.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    /**
     * StateFlow emitting permission status. true if PACKAGE_USAGE_STATS permission is granted,
     * false otherwise.
     */
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    /**
     * Checks permission status and fetches usage data if granted.
     *
     * This should be called:
     * - On initial screen load (onCreate)
     * - When returning from settings (onResume)
     */
    fun checkPermissionAndFetchData() {
        val hasPerm = repository.hasUsagePermission()
        _hasPermission.value = hasPerm
        if (hasPerm) {
            fetchUsageStats()
        }
    }

    /**
     * Fetches usage statistics from repository on IO dispatcher.
     *
     * Although UsageStatsManager operations are relatively fast, we use Dispatchers.IO to avoid
     * blocking the main thread, especially when processing many apps.
     */
    private fun fetchUsageStats() {
        viewModelScope.launch {
            // Execute repository call on IO thread to avoid blocking UI
            val stats =
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        repository.getUsageStats()
                    }
            _usageList.value = stats
        }
    }

    /**
     * Opens system settings page for granting usage access permission.
     *
     * Navigates to: Settings > Special App Access > Usage Access User must manually toggle
     * permission for this app.
     */
    fun openUsageSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }
}
