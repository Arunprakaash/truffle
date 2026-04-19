package com.truffleapp.truffle.data

fun emptyLedgerData(
    userName: String = "",
    accounts: List<Account> = emptyList(),
) = LedgerData(
    user               = User(firstName = userName),
    netWorth           = 0.0,
    netWorthLastMonth  = 0.0,
    inflow             = 0.0,
    outflow            = 0.0,
    accounts           = accounts,
    transactions       = emptyList(),
    bills              = emptyList(),
    goals              = emptyList(),
    budgets            = emptyList(),
    weeklyFlow         = emptyList(),
)
