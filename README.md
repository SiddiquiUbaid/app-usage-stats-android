# App Usage Stats

A Digital Wellbeing utility app for Android that tracks and displays daily application usage statistics.

## Features

- **Accurate Daily Tracking**: Shows app usage time from midnight to now
- **Clean Material3 UI**: Simple, modern interface with app icons and usage times
- **Privacy-Focused**: All data stays on device, uses Android's UsageEvents API
- **Smart Filtering**: Only shows user-facing apps, filters out system services

## Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture:

```
┌─────────────────┐
│   MainActivity  │  ← Entry point, observes ViewModel state
└────────┬────────┘
         │
┌────────▼────────┐
│  MainViewModel  │  ← Manages UI state, coordinates data fetching
└────────┬────────┘
         │
┌────────▼─────────────┐
│ AppUsageRepository   │  ← Fetches usage data via UsageEvents API
└──────────────────────┘
```

### Key Components

#### Data Layer
- **`AppUsageRepository`**: Fetches usage statistics using `UsageStatsManager.queryEvents()`
- **`AppUsageInfo`**: Data class representing usage info for a single app

#### ViewModel Layer
- **`MainViewModel`**: Exposes StateFlow for UI state, handles permission checks

#### UI Layer
- **`MainActivity`**: Compose-based activity, observes ViewModel
- **`UsageListScreen`**: Main composable with three states (permission request, no data, data list)
- **`AppUsageItem`**: Card displaying individual app usage

## How It Works

### UsageEvents API (Not UsageStats!)

This app uses **`queryEvents()`** instead of `queryUsageStats()` for accuracy:

1. **Query events** from midnight to now
2. **Track ACTIVITY_RESUMED** (app comes to foreground)
3. **Track ACTIVITY_PAUSED** (app goes to background)
4. **Calculate duration** = pause timestamp - resume timestamp
5. **Aggregate** all sessions per app

This approach matches Android's Digital Wellbeing behavior and avoids the "ghost app" issue with pre-aggregated stats.

### Permission Handling

The app requires **`PACKAGE_USAGE_STATS`** permission:
- Cannot be granted programmatically
- User must enable in: Settings > Special App Access > Usage Access
- App detects permission status on resume and refreshes data

## Setup & Build

### Prerequisites
- Android Studio Hedgehog or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36 (Android 15)

### Build Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on device/emulator

### Permissions Required

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

## Project Structure

```
app/src/main/java/com/learning/appusagestats/
├── data/
│   └── AppUsageRepository.kt    # Data fetching logic
├── ui/
│   ├── UsageListScreen.kt       # Compose UI screens
│   └── theme/                   # Material3 theme
├── MainActivity.kt              # Entry point
└── MainViewModel.kt             # State management
```

## Future Enhancements

- [ ] Weekly/Monthly usage trends
- [ ] Pie chart visualization
- [ ] Focus mode with app limits
- [ ] Export data to CSV
- [ ] Dependency injection (Hilt)

## License

This project is for educational purposes.
