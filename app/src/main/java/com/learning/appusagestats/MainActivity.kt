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
import androidx.lifecycle.ViewModelProvider
import com.learning.appusagestats.ui.UsageListScreen
import com.learning.appusagestats.ui.theme.AppUsageStatsTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel // imprvmnt: this is a tradition way
    // to initializ a ViewModel in an Activity,
    // 'by viewModels' delegate is the latest preferred.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            AppUsageStatsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissionAndFetchData()
    }
}
