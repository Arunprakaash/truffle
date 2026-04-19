package com.truffleapp.truffle.data

fun emptyLedgerData(
    userName: String = "",
    accounts: List<Account> = emptyList(),
    displayCurrency: String = DEFAULT_LEDGER_CURRENCY,
) = LedgerData(
    user               = User(firstName = userName),
    netWorth           = 0.0,
    netWorthLastMonth  = 0.0,
    inflow             = 0.0,
    outflow            = 0.0,
    displayCurrency    = normalizeLedgerCurrencyCode(displayCurrency),
    accounts           = accounts,
    transactions       = emptyList(),
    bills              = emptyList(),
    goals              = emptyList(),
    budgets            = emptyList(),
    weeklyFlow         = emptyList(),
)
