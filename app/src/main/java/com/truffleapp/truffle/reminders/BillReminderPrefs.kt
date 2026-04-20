package com.truffleapp.truffle.reminders

import android.content.Context

private const val PREFS = "truffle_notification_prefs"
private const val KEY_BILL_REMINDERS = "bill_reminders_enabled"

/** Bill reminder notifications are on by default; user can turn off in Data & backup. */
object BillReminderPrefs {
    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_BILL_REMINDERS, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BILL_REMINDERS, enabled)
            .apply()
    }
}
