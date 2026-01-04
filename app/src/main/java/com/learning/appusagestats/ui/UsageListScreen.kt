package com.learning.appusagestats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.learning.appusagestats.R
import com.learning.appusagestats.data.AppUsageInfo
import com.learning.appusagestats.util.TimeUtils

/**
 * Main screen displaying app usage statistics or permission request.
 *
 * This screen has three states:
 * 1. Permission not granted - Shows permission request UI
 * 2. Permission granted, no data - Shows "no data" message
 * 3. Permission granted, has data - Shows scrollable list of apps
 *
 * @param usageList List of app usage information to display
 * @param hasPermission Whether PACKAGE_USAGE_STATS permission is granted
 * @param onGrantPermissionClick Callback when user clicks "Grant Permission" button
 */
@Composable
fun UsageListScreen(
        usageList: List<AppUsageInfo>,
        hasPermission: Boolean,
        onGrantPermissionClick: () -> Unit
) {
    if (!hasPermission) {
        // State 1: Permission request UI
        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = stringResource(R.string.permission_required_title),
                    style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = stringResource(R.string.permission_required_desc),
                    style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onGrantPermissionClick) {
                Text(stringResource(R.string.grant_permission_btn))
            }
        }
    } else {
        if (usageList.isEmpty()) {
            // State 2: No usage data available
            Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
            ) { Text(stringResource(R.string.no_data_message)) }
        } else {
            // State 3: Display usage list
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) { items(usageList) { appInfo -> AppUsageItem(appInfo) } }
        }
    }
}


/**
 * Card displaying usage information for a single app.
 *
 * Shows:
 * - App icon (48dp)
 * - App name (bold)
 * - Formatted usage time (e.g., "2h 15m" or "45m")
 *
 * @param appInfo The app usage data to display
 */
@Composable
fun AppUsageItem(appInfo: AppUsageInfo) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon or placeholder
            if (appInfo.icon != null) {
                Image(
                    bitmap = appInfo.icon.toBitmap().asImageBitmap(),
                    contentDescription = appInfo.appName,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // App name and usage time
            Column {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = TimeUtils.formatTime(appInfo.timeInForeground),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
