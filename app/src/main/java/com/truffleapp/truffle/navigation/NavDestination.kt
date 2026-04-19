package com.truffleapp.truffle.navigation

// Each entry maps 1-to-1 with a bottom nav item.
// The `label` is displayed under the icon (uppercase in the UI layer).
enum class NavDestination(val label: String) {
    Today("Today"),
    Accounts("Accounts"),
    Flow("Flow"),
    Goals("Goals"),
}
