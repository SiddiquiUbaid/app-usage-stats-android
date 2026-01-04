package com.learning.appusagestats

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learning.appusagestats.data.AppUsageInfo
import com.learning.appusagestats.data.AppUsageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
 * @property repository Repository to fetch usage data
 * @property ioDispatcher Dispatcher for IO operations (injected for testing)
 * ```
 *                            But for simplicity in this small app we can keep AndroidViewModel or
 *                            pass Application context.
 *                            Wait, passing Application is fine for AndroidViewModel but limits unit testing.
 *                            Let's switch to standard ViewModel and abstract the Intent creation if possible.
 *                            For now, let's inject Application only for Intent starting or pass a navigator.
 *                            To be unit testable, we should avoid Android classes.
 *                            But `startActivity` needs Context.
 *                            Let's keep AndroidViewModel but inject Repository.
 * ```
 */
class MainViewModel(
        application: Application,
        private val repository: AppUsageRepository,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

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
            val stats = kotlinx.coroutines.withContext(ioDispatcher) { repository.getUsageStats() }
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
