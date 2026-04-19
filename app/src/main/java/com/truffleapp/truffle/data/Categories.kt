package com.truffleapp.truffle.data

data class CategoryInfo(val label: String, val icon: String)

val CATEGORIES: Map<String, CategoryInfo> = mapOf(
    "food"     to CategoryInfo("Food & dining",    "coffee"),
    "groc"     to CategoryInfo("Groceries",        "cart"),
    "home"     to CategoryInfo("Home",             "home2"),
    "trans"    to CategoryInfo("Transport",        "car"),
    "learn"    to CategoryInfo("Books & learning", "book"),
    "gifts"    to CategoryInfo("Gifts & giving",   "gift"),
    "wellness" to CategoryInfo("Wellness",         "tree"),
    "income"   to CategoryInfo("Income",           "arrowUp"),
    "transfer" to CategoryInfo("Transfer",         "arrowUp"),
)

// Keys shown in the recategorize picker (exclude income/transfer)
val RECATEGORIZABLE = CATEGORIES.filterKeys { it != "income" && it != "transfer" }
