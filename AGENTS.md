# Truffle — Agent Reference

> For any Claude Code agent continuing work on this project. Read this before touching any file.

---

## What this app is

**Truffle** is a personal finance Android app built with Jetpack Compose. The design system is called **Stillwater** — warm, minimal, serif-forward, calm. No Material scaffolding, no standard nav components. Every pixel is custom. The reference design lives in `/Users/arun/Downloads/finance os/` (HTML/JSX/CSS source files).

The app is **open source**, no backend, no paid APIs. All data is local.

---

## Current state (as of v1.2.0)

### Released
- **v1.0** — core screens, bottom nav, sample data
- **v1.1.0** — budgets, credit limits, bill payments, fund checks, multi-currency, signing, notifications
- **v1.2.0** — transaction location (GPS capture + OSM tile map), SheetButton component, Kotlin 2.3.20 / KSP 2.3.6 / Room 2.8.4

### Build command
```bash
JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home ./gradlew :app:compileDebugKotlin
JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home ./gradlew :app:assembleDebug
JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home ./gradlew :app:assembleRelease
```

Build is clean as of last session. Always run `compileDebugKotlin` first and fix all `e:` errors before assembling.

### Release APK
Output renamed to `truffle-release.apk`. Signed with keystore (properties in `keystore.properties`, not committed). See `e68e105` commit for signing guide.

---

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.3.20 |
| UI | Jetpack Compose (BOM 2025.04.01) |
| Architecture | Single Activity + ViewModel |
| Local DB | Room 2.8.4 (SQLite, schema v6) |
| Background | WorkManager 2.10.0 (bill reminders) |
| Image loading | Coil 2.7.0 (OSM tile fetch in TxDetailSheet) |
| Fonts | Bundled TTF in `res/font/` — Inter (sans) + Cormorant Garamond (serif) |
| Icons | Material Icons Extended |
| Min SDK | 33 (Android 13) |
| Target SDK | 36 |

No Hilt, no Navigation component, no Retrofit. Deliberately minimal.

---

## Project layout

```
app/src/main/java/com/truffleapp/truffle/
├── TruffleApplication.kt          — App class; sets up notification channel + reminder scheduling
├── MainActivity.kt                — Single activity; all navigation, all sheet orchestration
│
├── navigation/
│   └── NavDestination.kt          — Enum: Today, Accounts, Flow, Goals
│
├── data/
│   ├── Models.kt                  — Core domain types: Transaction, Bill, Account, Goal, Budget, LedgerData
│   ├── Categories.kt              — CATEGORIES map + RECATEGORIZABLE (icon keys → labels)
│   ├── LedgerCurrency.kt          — ISO currency list, formatting helpers, normalization
│   ├── LedgerDerivations.kt       — Derived state: budget spend tallies, weekly flow, net worth delta
│   ├── EmptyData.kt               — emptyLedgerData() factory (used after onboarding / on fresh install)
│   ├── ImportBackupResult.kt      — Sealed result type for JSON backup import
│   ├── ReflectionPicker.kt        — Daily intention copy picker
│   ├── SampleData.kt              — Static seed data (used in Compose Previews only, NOT in the live app)
│   └── db/
│       ├── LedgerDatabase.kt      — Room DB singleton, schema v6, 5 migrations
│       ├── LedgerEntities.kt      — @Entity classes mirroring domain models
│       ├── LedgerDao.kt           — DAO queries
│       └── LedgerRepository.kt    — Single source of truth: load/persist/export/import/clear
│
├── reminders/
│   ├── BillReminderWorker.kt      — WorkManager job; posts bill summary notification
│   ├── BillReminderScheduler.kt   — Schedules / cancels periodic reminder work
│   ├── BillReminderNotifications.kt — Builds and posts notification
│   ├── BillReminderPrefs.kt       — SharedPrefs for reminder enabled toggle
│   └── BootCompletedReceiver.kt   — Reschedules reminders after device reboot
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt               — 13 Stillwater color tokens (ColorPage, ColorInk, etc.)
│   │   ├── Type.kt                — InterFamily, CormorantFamily, SansFamily, SerifFamily, StillwaterType scale
│   │   └── Theme.kt               — StillwaterTheme (wraps MaterialTheme, no dark mode)
│   │
│   ├── components/                — All reusable composables
│   │   ├── Primitives.kt          — Hairline, Caps, IconCircle, SectionHeader, categoryIcon(), RingProgress, ProgressBar, SheetButton
│   │   ├── MoneyText.kt           — fmt(), MoneyText() — tabular serif money display
│   │   ├── BottomNavBar.kt        — Sliding pill indicator nav bar
│   │   ├── TxRow.kt               — Transaction list row
│   │   ├── BillRow.kt             — Bill list row
│   │   ├── AccountRow.kt          — Account list row
│   │   ├── IntentionCard.kt       — Daily reflection card (serif italic, curly quotes)
│   │   ├── TxDetailSheet.kt       — Transaction detail + recategorize + delete
│   │   ├── BillSheet.kt           — Bill view + mark paid
│   │   ├── AddToGoalSheet.kt      — Add funds to existing goal (slider + quick presets)
│   │   ├── AddTypeSheet.kt        — Picker: Transaction / Bill / Goal / Account
│   │   ├── AddTransactionSheet.kt — Full transaction entry form
│   │   ├── NewBillSheet.kt        — New bill creation form
│   │   ├── NewGoalSheet.kt        — New goal creation form
│   │   ├── NewAccountSheet.kt     — New account creation form
│   │   ├── EditAccountSheet.kt    — Edit existing account (name, balance, currency, credit limit)
│   │   ├── AccountBackupSheet.kt  — Settings sheet: currency, reminders, export/import/clear
│   │   ├── BudgetConfigSheet.kt   — Edit budget limits per category
│   │   └── CurrencySelector.kt    — Currency picker (list of 20 ISO currencies)
│   │
│   ├── screens/
│   │   ├── TruffleSplash.kt       — Animated splash (scale + fade, ~1s, respects reduced motion)
│   │   ├── OnboardingScreen.kt    — 2-step onboarding: Name → First account (Crossfade transition)
│   │   ├── TodayScreen.kt         — Journal layout: greeting, net worth card, this month, recent txs, upcoming bills
│   │   ├── AccountsScreen.kt      — Net worth header, Cash/Invest/Credit groups, edit/delete accounts
│   │   ├── FlowScreen.kt          — Tx list: week/month/year range, In/Out filter, date-grouped
│   │   └── GoalsScreen.kt         — Goal cards with RingProgress, saved/target, Add button
│   │
│   └── viewmodel/
│       └── LedgerViewModel.kt     — AndroidViewModel; owns all state mutations via LedgerRepository
```

---

## Design system rules

**Never break these:**

- Background: `ColorPage = #F5F1EB` (warm greige). Never white.
- Surface cards: `ColorSurface = #EDE8DF`, radius 14dp
- All money: `MoneyText()` composable — tabular serif, `fontFeatureSettings = "\"tnum\" on, \"lnum\" on"`
- All labels: `Caps()` — uppercase, Inter Medium, 0.14em tracking at 10sp
- Dividers: `Hairline()` — 0.5dp, `ColorBorderTertiary`
- No ripple anywhere — `indication = null` on all clickables
- No dark mode — `StillwaterTheme` is light only
- Bottom nav: sliding `ColorFeature2` pill via `drawBehind`, `weight(1f)` equal slots, `FastOutSlowInEasing` 320ms
- Fonts: `SerifFamily` = Cormorant Garamond, `SansFamily` = Inter (both bundled in `res/font/`)
- Nav bar bottom padding: all screens use `.padding(bottom = 100.dp)` to clear the floating nav

---

## Data flow

```
User action
    ↓
MainActivity (sheet state vars)
    ↓
LedgerViewModel.someMethod()
    ↓
LedgerRepository.persist(newData)   ← writes to Room + updates in-memory LedgerData
    ↓
viewModel.data (mutableStateOf)     ← triggers Compose recomposition
    ↓
All screens re-render
```

**Key invariant:** `LedgerRepository` is the only place that writes to Room. ViewModel calls repo, never Room directly.

---

## Persistence

- **Room DB** (`LedgerDatabase`, schema v6) — all entities: accounts, transactions, bills, goals, budgets, app metadata
- **SharedPreferences** (`ledger_prefs`) — onboarding flag, user name, initial accounts (legacy; new installs use Room)
- **No cloud, no backend**
- Backup/restore via JSON file export (share intent) — `LedgerRepository.exportBackupJson()` / `importBackupJson()`
- Schema version constant: `LEDGER_BACKUP_SCHEMA_VERSION` in `ImportBackupResult.kt`

---

## Notifications / Reminders

- **Permission**: `POST_NOTIFICATIONS` (Android 13+, requested at runtime from AccountBackupSheet)
- **Channel**: created in `TruffleApplication.onCreate()`
- **Worker**: `BillReminderWorker` — daily periodic job via WorkManager, checks bills due within 3 days
- **Scheduling**: `BillReminderScheduler.sync()` — call whenever reminder toggle changes or bills change
- **Boot**: `BootCompletedReceiver` reschedules after device restart

---

## Onboarding flow

```
App launch → TruffleSplash (~1s)
    ↓
viewModel.hasOnboarded?
    ├── false → OnboardingScreen
    │               Step 1: Name (Crossfade)
    │               Step 2: First account (name + Cash/Invest/Credit type)
    │               → viewModel.completeOnboarding(name, account)
    │               → hasOnboarded = true → main app
    └── true  → LedgerApp (Today screen)
```

---

## Multi-currency

- Each `Account` has a `currency: String` (ISO 4217 code)
- `LedgerData.displayCurrency` is the app-wide display currency (set in AccountBackupSheet)
- `MoneyText()` and `fmt()` both accept `currencyCode` parameter
- `LedgerCurrency.kt` — 20 supported currencies, `formatLedgerMoney()`, `ledgerCurrencySymbol()`
- Cross-currency display: transactions shown in account's currency, totals shown in display currency

---

## Location / Map

- `Transaction` has nullable `lat: Double?` and `lng: Double?` — null for all pre-v1.2 transactions
- Location captured at entry time via `LocationManager.getLastKnownLocation()` — no Google Play Services
- Permissions: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `INTERNET` (all in manifest)
- `TxDetailSheet` shows a 3×3 OSM tile grid at zoom 16 via Coil + `BoxWithConstraints` when lat/lng present
- Pin dot is positioned at exact sub-tile `fracX`/`fracY` offset, not tile centre
- OSM tile URL: `https://tile.openstreetmap.org/{zoom}/{x}/{y}.png` — User-Agent header required

---

## SheetButton component

`SheetButton(text, onClick, variant, icon?)` in `Primitives.kt` — full-width, no ripple.
- `Primary` — `ColorInk` bg, `ColorPage` text (submit/confirm)
- `Ghost` — `ColorSurface` bg, `ColorInk` text (neutral secondary)
- `Destructive` — `ColorFeature2` bg, `ColorTextSecondary` text (remove/delete)

---

## Known sharp edges

- `SampleData.kt` is **only for Compose `@Preview`**. It is NOT used in the live app (ViewModel starts from Room/empty).
- `weight(1f)` on nav items must be applied at the call site (RowScope extension), not inside the child composable.
- `animateFloatAsState` for `by` delegation requires `import androidx.compose.runtime.getValue`.
- Room migrations must be added sequentially — current schema is v6. Never skip a version.
- Bill reminder work is unique-named — always call `BillReminderScheduler.sync()` after bill mutations.
- `TxDetailSheet` has an `onRemove` callback wired to `viewModel.removeTransaction()` which also adjusts account balance.
- `addTransaction()` returns `Boolean` — false means insufficient funds. UI should check this.
- `markBillPaid()` returns `Boolean` — false means insufficient funds in linked account.

---

## What's next (not yet built)

- [ ] **Room migration**: SharedPreferences accounts → Room (partial; legacy migration exists in repo)
- [ ] **Edit transaction** (TxDetailSheet currently supports recategorize + delete only)
- [ ] **CSV / OFX import** (discussed, deferred — manual entry first)
- [ ] **Account balance history** (for sparkline on net worth card)
- [ ] **Budget screen** (BudgetConfigSheet exists; no dedicated budget overview screen yet)
- [ ] **Bill recurrence auto-advance** (model supports it; UI mark-paid advances dueDate but no auto-create next cycle)
- [ ] **Dark mode** (explicitly excluded from design spec)
- [ ] **Widget** (not discussed)
- [ ] **Full persistence of added accounts** beyond first onboarding account (works in-session; Room write happens)

---

## File conventions

- One composable per file for sheets and screens
- Private composables (sub-parts of a screen) live in the same file
- Shared primitives in `Primitives.kt`
- No comments unless the WHY is non-obvious
- No `Arrangement.SpaceAround` in the nav bar — items use `weight(1f)` instead
- Form inputs: `BasicTextField` with `decorationBox` bottom hairline — NOT `OutlinedTextField`
