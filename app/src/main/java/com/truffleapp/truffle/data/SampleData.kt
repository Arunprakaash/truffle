package com.truffleapp.truffle.data

import java.time.LocalDate

// Mirrors SEED_DATA from the Ledger reference (data.jsx).
// Used for Compose previews and initial app state.

private val SAMPLE_ANCHOR = LocalDate.of(2026, 4, 19)
private val SAMPLE_TX_ANCHOR: Long = SAMPLE_ANCHOR.toEpochDay()

val SampleData = LedgerData(
    user = User("Sasha"),

    netWorth          = 184_420.0,
    netWorthLastMonth = 179_885.0,
    inflow            = 7_820.0,
    outflow           = 3_462.0,
    displayCurrency   = DEFAULT_LEDGER_CURRENCY,

    accounts = listOf(
        Account("chk", "Checking",    "First Federal",  8_340.12,   AccountKind.Cash),
        Account("sav", "Savings",     "First Federal",  24_118.40,  AccountKind.Cash),
        Account("hys", "High-yield",  "Marcus",         18_000.00,  AccountKind.Cash),
        Account("brk", "Brokerage",   "Fidelity",       72_340.55,  AccountKind.Invest),
        Account("ret", "Retirement",  "Fidelity",       63_921.88,  AccountKind.Invest),
        Account("crd", "Sapphire",    "Chase",          -2_300.38,  AccountKind.Credit),
    ),

    transactions = listOf(
        Transaction("t01", "Today",     "8:14 am",  "Blue Bottle",       "Coffee, on foot",        -5.75,   "food",     "coffee",   "Sapphire",      SAMPLE_TX_ANCHOR),
        Transaction("t02", "Today",     "8:02 am",  "Morning transfer",  "Into savings, as usual",  200.0,   "transfer", "arrowUp",  "Savings",       SAMPLE_TX_ANCHOR - 1),
        Transaction("t03", "Yesterday", "7:41 pm",  "Kinokuniya",        "Two books",              -38.20,  "learn",    "book",     "Sapphire",      SAMPLE_TX_ANCHOR - 5),
        Transaction("t04", "Yesterday", "1:12 pm",  "Whole Foods",       "Groceries for the week", -84.36,  "groc",     "cart",     "Sapphire",      SAMPLE_TX_ANCHOR - 5),
        Transaction("t05", "Monday",    "6:30 pm",  "Fig & Olive",       "Dinner with Mae",        -62.00,  "food",     "coffee",   "Sapphire",      SAMPLE_TX_ANCHOR - 9),
        Transaction("t06", "Monday",    "9:00 am",  "Paycheck",          "Monthly, as agreed",    5_200.0,  "income",   "arrowUp",  "Checking",      SAMPLE_TX_ANCHOR - 9),
        Transaction("t07", "Sunday",    "3:42 pm",  "Farmer\u2019s market", "Flowers, bread, peaches", -28.00, "groc", "cart",    "Checking",      SAMPLE_TX_ANCHOR - 13),
        Transaction("t08", "Saturday",  "11:12 am", "Pilates studio",    "Class pack, 10 classes", -180.00, "wellness", "tree",     "Sapphire",      SAMPLE_TX_ANCHOR - 17),
        Transaction("t09", "Friday",    "8:20 pm",  "A2 Cinema",         "Late film alone",        -14.50,  "food",     "gift",     "Sapphire",      SAMPLE_TX_ANCHOR - 21),
        Transaction("t10", "Thursday",  "4:55 pm",  "Uber",              "Home in the rain",       -18.40,  "trans",    "car",      "Sapphire",      SAMPLE_TX_ANCHOR - 25),
        Transaction("t11", "Thursday",  "12:08 pm", "Sweetgreen",        "Lunch at desk",          -14.25,  "food",     "coffee",   "Sapphire",      SAMPLE_TX_ANCHOR - 25),
        Transaction("t12", "Wednesday", "7:01 am",  "Dividend",          "VTI, quarterly",          64.12,  "income",   "arrowUp",  "Brokerage",     SAMPLE_TX_ANCHOR - 33),
    ),

    bills = listOf(
        Bill("rent",     "Rent",     1_980.0, SAMPLE_ANCHOR.plusDays(8).toEpochDay(),  false, "Checking"),
        Bill("spot",     "Spotify",     11.0, SAMPLE_ANCHOR.plusDays(3).toEpochDay(),  false, "Sapphire"),
        Bill("gym",      "Gym",         42.0, SAMPLE_ANCHOR.plusDays(12).toEpochDay(), false, "Sapphire"),
        Bill("phone",    "Phone",       65.0, SAMPLE_ANCHOR.plusDays(18).toEpochDay(), false, "Checking"),
        Bill("internet", "Internet",    70.0, SAMPLE_ANCHOR.plusDays(22).toEpochDay(), true,  "Checking"),
    ),

    goals = listOf(
        Goal("sab",  "Sabbatical",         "3 months, no reason",      12_400.0, 24_000.0),
        Goal("home", "Home down payment",  "Somewhere with a porch",   38_200.0, 80_000.0),
        Goal("cush", "Rainy-day cushion",  "6 months of quiet",        18_000.0, 18_000.0),
        Goal("writ", "The writing desk",   "The one from Copenhagen",   1_240.0,  4_200.0),
    ),

    budgets = listOf(
        Budget("food",    "Food & dining",    "coffee",  412.0,  600.0),
        Budget("groc",    "Groceries",        "cart",    286.0,  450.0),
        Budget("home",    "Home",             "home2",  1980.0, 2100.0),
        Budget("trans",   "Transport",        "car",      94.0,  250.0),
        Budget("learn",   "Books & learning", "book",     62.0,  150.0),
        Budget("gifts",   "Gifts & giving",   "gift",    340.0,  300.0),
        Budget("wellness","Wellness",         "tree",    188.0,  220.0),
    ),

    weeklyFlow = listOf(
        WeeklyFlow("6 wk ago",  5_200.0, 3_120.0),
        WeeklyFlow("5 wk ago",      0.0, 3_680.0),
        WeeklyFlow("4 wk ago",  5_264.0, 2_910.0),
        WeeklyFlow("3 wk ago",     64.0, 3_440.0),
        WeeklyFlow("2 wk ago",  1_356.0, 3_020.0),
        WeeklyFlow("This week", 5_200.0, 3_462.0),
    ),
)
