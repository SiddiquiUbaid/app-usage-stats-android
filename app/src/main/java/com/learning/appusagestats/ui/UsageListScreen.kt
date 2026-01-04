package com.learning.appusagestats.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.learning.appusagestats.R
import com.learning.appusagestats.data.AppUsageInfo
import com.learning.appusagestats.util.ColorUtils
import com.learning.appusagestats.util.TimeUtils

/** Main screen displaying app usage statistics in a dashboard layout. */
@Composable
fun UsageListScreen(
        usageList: List<AppUsageInfo>,
        hasPermission: Boolean,
        onGrantPermissionClick: () -> Unit
) {
    if (!hasPermission) {
        PermissionRequestSection(onGrantPermissionClick)
    } else {
        if (usageList.isEmpty()) {
            NoDataSection()
        } else {
            DashboardLayout(usageList)
        }
    }
}

@Composable
fun PermissionRequestSection(onGrantPermissionClick: () -> Unit) {
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
}

@Composable
fun NoDataSection() {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) { Text(stringResource(R.string.no_data_message)) }
}

@Composable
fun DashboardLayout(usageList: List<AppUsageInfo>) {
    // Show top 3 apps in graph for clarity, or all? Let's show top 5 for better visualization
    val topApps = remember(usageList) { usageList.take(5) }

    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Usage Overview Section (Graph)
        DashboardGraphSection(topApps, usageList)

        // 2. App List Section
        AppUsageListSection(usageList)
    }
}

@Composable
fun DashboardGraphSection(topApps: List<AppUsageInfo>, allApps: List<AppUsageInfo>) {
    var isPieChart by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Dropdown
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Usage Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                )

                Box {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = true }.padding(8.dp)
                    ) {
                        Text(
                                text = if (isPieChart) "Pie Chart" else "Progress Bar",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Chart Type",
                                tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                                text = { Text("Pie Chart") },
                                onClick = {
                                    isPieChart = true
                                    expanded = false
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Progress Bar") },
                                onClick = {
                                    isPieChart = false
                                    expanded = false
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Graph Content
            Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentAlignment = Alignment.Center
            ) {
                if (isPieChart) {
                    PieChart(topApps)
                } else {
                    ProgressBarChart(topApps)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend for top apps
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                topApps.take(3).forEach { app ->
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(
                                modifier =
                                        Modifier.size(12.dp)
                                                .background(
                                                        ColorUtils.getAppColor(app.packageName),
                                                        shape = RoundedCornerShape(2.dp)
                                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = app.appName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(apps: List<AppUsageInfo>) {
    val totalTime = apps.sumOf { it.timeInForeground }
    val animatedProgress = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(key1 = apps) {
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    if (totalTime == 0L) return

    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = -90f
        val strokeWidth = 30.dp.toPx()

        apps.forEach { app ->
            val sweepAngle =
                    (app.timeInForeground.toFloat() / totalTime) * 360f * animatedProgress.value

            drawArc(
                    color = ColorUtils.getAppColor(app.packageName),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }

        // Draw total time in center
        // Note: Drawing text in Canvas requires TextMeasurer (Compose 1.3+).
        // For simplicity and compatibility, we overlay a Text composable in the Box instead.
    }

    // Overlay Total Time
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Total", style = MaterialTheme.typography.labelMedium)
        Text(
                text = TimeUtils.formatTime(totalTime),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProgressBarChart(apps: List<AppUsageInfo>) {
    val maxTime = apps.maxOfOrNull { it.timeInForeground }?.toFloat() ?: 1f

    Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        apps.forEach { app ->
            val progress = (app.timeInForeground / maxTime)
            val animatedProgress by
                    animateFloatAsState(
                            targetValue = progress,
                            animationSpec = tween(durationMillis = 1000)
                    )

            Column {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = app.appName, style = MaterialTheme.typography.bodySmall)
                    Text(
                            text = TimeUtils.formatTime(app.timeInForeground),
                            style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(8.dp)
                                        .background(
                                                Color.LightGray.copy(alpha = 0.3f),
                                                RoundedCornerShape(4.dp)
                                        )
                ) {
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth(animatedProgress)
                                            .height(8.dp)
                                            .background(
                                                    ColorUtils.getAppColor(app.packageName),
                                                    RoundedCornerShape(4.dp)
                                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AppUsageListSection(usageList: List<AppUsageInfo>) {
    var expanded by remember { mutableStateOf(false) }
    // Show top 5 initially
    val displayedList = if (expanded) usageList else usageList.take(5)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "Your Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )

            OutlinedButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Collapse" else "Expand All")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        displayedList.forEach { appInfo ->
            AppUsageItem(appInfo)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AppUsageItem(appInfo: AppUsageInfo) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            if (appInfo.icon != null) {
                Image(
                        bitmap = appInfo.icon.toBitmap().asImageBitmap(),
                        contentDescription = appInfo.appName,
                        modifier = Modifier.size(40.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                    text = TimeUtils.formatTime(appInfo.timeInForeground),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
