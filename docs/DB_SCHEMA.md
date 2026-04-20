# Truffle Database Schema (Room v6)

This document reflects the current Room database schema defined in:

- `app/src/main/java/com/truffleapp/truffle/data/db/LedgerDatabase.kt`
- `app/src/main/java/com/truffleapp/truffle/data/db/LedgerEntities.kt`

Database file name: `truffle_ledger.db`  
Schema version: `6`

## ER Diagram (Mermaid)

```mermaid
erDiagram
    APP_META {
        INT id PK
        TEXT user_first_name
        BOOLEAN has_onboarded
        TEXT nw_snap_ym
        TEXT nw_snap_nw
        TEXT nw_baseline
        TEXT display_currency
    }

    ACCOUNTS {
        TEXT id PK
        TEXT name
        TEXT institution
        REAL balance
        TEXT kind
        TEXT currency
        REAL credit_limit
    }

    TRANSACTIONS {
        TEXT id PK
        TEXT date
        TEXT time
        TEXT merchant
        TEXT note
        REAL amount
        TEXT category
        TEXT icon
        TEXT account
        INTEGER recorded_epoch_day
        REAL lat
        REAL lng
    }

    BILLS {
        TEXT id PK
        TEXT label
        REAL amount
        INTEGER due_date_epoch
        BOOLEAN paid
        TEXT account
        TEXT recurrence
    }

    GOALS {
        TEXT id PK
        TEXT title
        TEXT note
        REAL saved
        REAL target
    }

    BUDGETS {
        TEXT id PK
        TEXT label
        TEXT icon
        REAL spent
        REAL limit
    }

    %% Logical relations (no Room FK constraints declared)
    ACCOUNTS ||--o{ TRANSACTIONS : "name -> account"
    ACCOUNTS ||--o{ BILLS : "name -> account"
    BUDGETS ||--o{ TRANSACTIONS : "id -> category"
```

## Relationship Notes

- `accounts` -> `transactions`: logical one-to-many via `transactions.account` matching `accounts.name`.
- `accounts` -> `bills`: logical one-to-many via `bills.account` matching `accounts.name`.
- `budgets` -> `transactions`: logical one-to-many via `transactions.category` matching `budgets.id`.
- `app_meta` is effectively a singleton table (`id = 1`) for app-level state and onboarding metadata.
- `goals` has no persisted foreign-key relation to `accounts` in the Room schema.

## Important Constraints / Behavior

- No explicit Room `@ForeignKey` constraints are defined in entities.
- `LedgerDao.replaceAll()` rewrites all core tables in one transaction (`accounts`, `transactions`, `bills`, `goals`, `budgets`, and `app_meta`).
- Budget `spent` values are derived/recomputed from transaction outflows by category in `LedgerDerivations.kt`.
- `transactions.lat` and `transactions.lng` are nullable (added in migration `5 -> 6`).
