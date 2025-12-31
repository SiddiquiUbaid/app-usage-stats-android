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
// impr: introduce di later
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppUsageRepository(application)

    private val _usageList = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val usageList: StateFlow<List<AppUsageInfo>> = _usageList.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    fun checkPermissionAndFetchData() {
        val hasPerm = repository.hasUsagePermission()
        _hasPermission.value = hasPerm
        if (hasPerm) {
            fetchUsageStats()
        }
    }

    private fun fetchUsageStats() {
        viewModelScope.launch {
            // Fetching in IO thread is handled by repository logic if needed,
            // but UsageStatsManager is fast enough for main thread usually,
            // still better to wrap in Dispatchers.IO for safety in real app.
            // For this simple example, we'll keep it simple as repository operations are
            // synchronous.
            // But to be correct let's just call it.
            // To be strictly correct we should probably emit from a background thread if it was
            // heavy.
            // Since `queryUsageStats` is synchronous, let's wrap it here to be safe.
            val stats =
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        repository.getUsageStats()
                    }
            _usageList.value = stats
        }
    }

    fun openUsageSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }
}
