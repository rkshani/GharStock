# Ghar Stock

Offline Android app for home grocery lists, pantry stock, expiry reminders and family medicine reminders.

- 100% offline: no internet permission, no ads, no analytics
- Kotlin, Jetpack Compose, Material 3 (light theme only), English only
- Min SDK 24, target SDK 37

## Features

| Area | What it does |
|---|---|
| Shopping | Multiple lists, items with quantity/unit/category/price, tick as bought, move bought items to stock |
| Stock | Pantry items with category, storage place, price, purchase and expiry dates; search, filters, sort by expiry, expiry colours, add to shopping list |
| Expiry reminders | Daily 9 AM WorkManager job, one grouped notification for expired and soon-to-expire stock and medicines |
| Medicines | Family members, four schedule types, exact alarm reminders with Taken/Skip, pill count, low pill alert, dose history |
| Home | Counts, today's doses with Taken/Skip, expiring soon |
| Report | Monthly grocery spending by category (Compose bar chart) |
| Backup | JSON export/import with the Storage Access Framework, confirmation before replacing data |

## Architecture

MVVM + Clean Architecture, packaged by feature.

```
app/src/main/java/com/rk/gharstock/
├── core/
│   ├── database/      Room entities, DAOs, converters, default seed data
│   ├── di/            Hilt modules
│   ├── domain/        Shared domain: validation, transactions, expiry alerts
│   ├── model/         Shared models (units, categories, expiry status)
│   ├── notification/  Channels, expiry worker, dose alarms and receivers
│   ├── preferences/   DataStore
│   ├── ui/            Theme and shared Compose components
│   └── util/
├── feature/
│   ├── home/ shopping/ stock/ medicine/ report/ backup/ settings/
│   │   ├── domain/    Models, repository interfaces, use cases
│   │   ├── data/      Repository implementations
│   │   └── …          Screens + ViewModels (one UiState and onEvent() per screen)
└── navigation/        Bottom navigation and app nav host
```

## Build

Requires JDK 17+ and the Android SDK.

```bash
./gradlew assembleDebug
./gradlew test
./gradlew assembleRelease
```

### Release signing

Create `keystore.properties` in the project root (it is git-ignored):

```properties
storeFile=path/to/upload-keystore.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

Without this file the release APK/AAB is built unsigned.

## Tests

Unit tests run on the JVM with Robolectric and an in-memory Room database (DAOs, use cases,
backup round trip) plus plain tests for schedule, expiry and filter logic.
