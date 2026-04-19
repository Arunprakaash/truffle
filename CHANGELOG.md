# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.0] — 2026-04-19

First public release. Truffle is a **local-first** money journal: your ledger lives in a **Room** database on the device; there is **no** sync server or bundled analytics.

### Highlights

- **Today** — Day-at-a-glance journal with spending context and quick paths into the rest of the app.
- **Flow** — Transaction list and detail; browse money in and out over time.
- **Goals** — Goals you fund from real activity; add money toward a goal from the ledger.
- **Accounts** — Accounts, balances, and **bills** (including **due dates** stored on-device).
- **Onboarding** — First-run setup so you are not dropped into an empty shell without context.
- **Splash** — Branded launch into the main experience.

### Data & backup

- **Export backup** — Share or archive a JSON snapshot of accounts, transactions, bills, goals, and budgets.
- **Import backup** — Replace the whole ledger from a file, with a **confirmation** step and a **short preview** (counts / label) so you know what you are about to apply.
- **Clear data** — Reset the app’s ledger when you want to start over (export first if you need a copy).
- **Account cleanup** — Deleting or renaming accounts moves dependent bills to **Unassigned** so nothing is left in an invalid state.

### Distribution & build

- **Android 13+** (`minSdk` 33), targets current SDK from `app/build.gradle.kts`.
- **Release builds** use **R8** code shrinking and **resource shrinking** for a smaller APK when you run `assembleRelease` (see `README.md` for signing).

### Known limitations

- **No Play Store listing** in this repo; install by sideloading the APK you attach to a GitHub Release (or build yourself). Updates only apply cleanly when signed with the **same** release key as the installed build.
- **Import replaces the entire ledger** on that device after you confirm—always **export** first if the current data matters.

[1.0.0]: https://github.com/Arunprakaash/truffle/releases/tag/v1.0.0
