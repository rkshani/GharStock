# Ghar Stock

Offline Android app for home grocery lists, pantry stock, expiry reminders and family medicine reminders.

- 100% offline: no internet permission, no ads, no analytics
- Kotlin, Jetpack Compose, Material 3 (light theme)
- MVVM + Clean Architecture, Hilt, Room, Coroutines/Flow, Navigation Compose, DataStore, WorkManager

## Build

Requires JDK 17+ and the Android SDK.

```bash
./gradlew assembleDebug
./gradlew test
```

## Structure

```
app/src/main/java/com/rk/gharstock/
├── core/        # database, di, ui (theme, components), notification, util
├── feature/     # home, shopping, stock, medicine, report, settings
└── navigation/  # bottom navigation and app nav host
```
