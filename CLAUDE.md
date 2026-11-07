# Ghar Stock — Build Instructions for Claude Code

You are building **Ghar Stock**, an offline Android app for home grocery, pantry stock, expiry reminders and family medicine reminders.
Build it **phase by phase**. Finish one phase fully, make sure it builds, then stop and wait for my "go" before the next phase.

---

## 1. Project Info

- **App name:** Ghar Stock
- **Package:** `com.rk.gharstock`
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Min SDK:** 24 (Android 7) · **Target/Compile SDK:** latest stable
- **Theme:** Light only (no dark mode)
- **App language:** English only
- **Data:** 100% offline. No internet permission. No ads. No analytics. Free app.
- **Target:** Play Store release

## 2. Tech Stack

- Architecture: **MVVM + Clean Architecture** (`data` / `domain` / `presentation` layers)
- DI: **Hilt**
- Database: **Room** (with migrations, no destructive migration in release)
- Async: **Coroutines + Flow**
- Navigation: **Navigation Compose** (type-safe routes)
- Background: **WorkManager** (daily expiry check) + **AlarmManager** exact alarms (medicine doses)
- Preferences: **DataStore**
- Serialization: **kotlinx.serialization** (for backup)
- Build: Gradle Kotlin DSL + **version catalog** (`libs.versions.toml`)
- Tests: JUnit, Turbine, Room in-memory DB tests for DAOs and use cases

## 3. Coding Rules

- Package by feature: `feature/shopping`, `feature/stock`, `feature/medicine`, `feature/report`, `feature/settings`, plus `core/` (database, di, ui, notification, util).
- One `UiState` data class per screen, exposed as `StateFlow`. UI events go through one `onEvent()` function.
- Repositories: interface in `domain`, implementation in `data`.
- No business logic in Composables.
- All strings in `strings.xml`. All dates stored as `LocalDate` / epoch millis via TypeConverters.
- Every screen must handle: empty state, loading state, delete confirmation dialog.
- Clean, readable code. Small functions. No unused code.
- After each phase: run `./gradlew assembleDebug` and `./gradlew test`. Fix all errors before finishing.

---

## 4. Git Workflow (VERY IMPORTANT)

- **Phase 0:** You only create the project. **I will push it to GitHub `main` myself.** Do not push in Phase 0.
- **After every screen or major change**, you must:
  1. Create a **new branch** from the latest work:
     - If previous branch is merged: `git checkout main && git pull` then branch from `main`.
     - If previous branch is not merged yet: branch from the previous branch and tell me.
  2. Branch naming: `feature/<short-name>`, `fix/<short-name>`, `chore/<short-name>`
     Examples: `feature/database-setup`, `feature/shopping-lists`, `feature/stock-screen`
  3. Make small, clear commits (Conventional Commits): `feat: add shopping list screen`, `fix: reminder time bug`
  4. Push: `git push -u origin <branch-name>`
  5. Tell me the branch name and a short summary of what changed.
- **Never push to `main`. Never force push. Never merge branches yourself.**
- Make sure the project builds before every push.
- Do not commit `local.properties`, `build/`, `.idea/` (keep a proper `.gitignore`).

---

## 5. Features (Final Scope)

### Shopping Lists
- Multiple lists (e.g. Kiryana, Medical Store, Sabzi). Create, rename, delete list.
- Items: name, quantity, unit, category, optional price. Tick (bought), edit, delete.
- "Move bought items to Stock" option.
- Low stock is **manual**: user adds item to a list from the stock screen ("Add to shopping list" button).

### Home Stock (Pantry)
- Item fields: name, quantity, unit, category, storage place, price, purchase date, expiry date (optional).
- Categories and storage places: **default list + user can add/edit/delete their own**.
  - Default categories: Grains, Dairy, Vegetables, Fruits, Spices, Snacks, Drinks, Cleaning, Other
  - Default places: Fridge, Freezer, Kitchen, Store Room
- Search, filter by category/place, sort by expiry.
- Expiry color: red = expired, orange = within 3 days, normal = safe.

### Expiry Reminders
- Notification **3 days before** expiry, at **9:00 AM**. Also show expired items.
- Daily WorkManager job at 9 AM checks and sends one grouped notification.

### Medicine
- **Family members**: add/edit/delete (name, optional relation).
- Medicine fields: name, member, dose amount, total pills/stock, expiry date, notes.
- Dose schedule types (all supported):
  1. Fixed times per day (e.g. 8 AM, 2 PM, 10 PM)
  2. Every X hours (with start time)
  3. Specific days of week (+ times)
  4. Course duration (start date + number of days)
- Dose notification actions: **Taken** and **Skip**. Taken reduces pill count.
- Dose history (taken/skipped log per medicine).
- Low pills alert when pills left ≤ 3 days of doses.
- Medicine expiry also goes into expiry reminders.
- Reschedule all alarms after phone reboot (`BOOT_COMPLETED`) and time change.
- Handle Android 13+ notification permission and Android 12+ exact alarm permission.

### Home Screen (Dashboard)
- "Expiring Soon" items
- "Today's Medicines" with Taken/Skip
- Quick counts: items in stock, shopping items pending
- Bottom navigation: Home, Shopping, Stock, Medicine, More

### Monthly Report
- Monthly grocery spending total (from stock item price + purchase date).
- Month picker, total, category-wise breakdown (simple bar chart in Compose, no heavy library).

### Backup
- **Export** all data to a local JSON file (Storage Access Framework, user picks location).
- **Import** from that file (confirm before replacing data).

### Settings (More tab)
- Manage categories, storage places, family members
- Backup / restore
- Notification permission status
- About screen (version)

---

## 6. Phases

### Phase 0 — Project Setup (no push)
- Create the project with the stack above, version catalog, Hilt, Room, Navigation, DataStore.
- Folder structure, light Material 3 theme (colors, typography, shapes), app icon placeholder.
- Bottom navigation with empty placeholder screens.
- `.gitignore`, `README.md` (short).
- `git init` and first commit **only**. Then stop and tell me to push to GitHub.

### Phase 1 — Database & Domain → branch `feature/database-setup`
- Room entities, DAOs, TypeConverters, relations:
  ShoppingList, ShoppingItem, StockItem, Category, StoragePlace, FamilyMember, Medicine, DoseSchedule, DoseLog
- Seed default categories and places on first launch.
- Domain models, repositories, use cases.
- DAO and use case unit tests.

### Phase 2 — Shopping Lists → branch `feature/shopping-lists`
- Lists screen, list detail screen, add/edit item bottom sheet.

### Phase 3 — Stock Screen → branch `feature/stock-screen`
- Stock list, add/edit item screen with date pickers, search, filter, sort, expiry colors, "Add to shopping list", "Move bought items to stock".

### Phase 4 — Categories, Places & Family Members → branch `feature/manage-settings`
- Manage screens in the More tab.

### Phase 5 — Expiry Reminders → branch `feature/expiry-reminders`
- Notification channels, permission flow, WorkManager daily 9 AM job.

### Phase 6 — Medicine Screens → branch `feature/medicine-screens`
- Medicine list (grouped by member), add/edit medicine with all 4 schedule types, medicine detail with dose history.

### Phase 7 — Medicine Reminders → branch `feature/medicine-reminders`
- Exact alarms, notification with Taken/Skip actions, pill count update, low pills alert, reboot/time change rescheduling.

### Phase 8 — Home Dashboard → branch `feature/home-dashboard`

### Phase 9 — Monthly Report → branch `feature/monthly-report`

### Phase 10 — Backup & Restore → branch `feature/backup-restore`

### Phase 11 — Polish & Release Prep → branch `chore/release-prep`
- Empty states, animations, accessibility (content descriptions, touch sizes), edge cases.
- R8/ProGuard rules, release build config, app icon, versionCode/versionName.
- Final test run. Short Play Store description in `docs/store-listing.md`.

---

## 7. How to Work

- Start with **Phase 0** now. Before writing code, show me a short plan for the phase.
- At the end of each phase give me:
  - Branch name
  - What was added (short bullet list)
  - Anything I need to test manually
- Then **wait for me** before starting the next phase.
- If something is unclear, ask me first. Do not guess big decisions.
