package com.truffleapp.truffle.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        BillReminderNotifications.ensureChannel(context.applicationContext)
        if (BillReminderPrefs.isEnabled(context.applicationContext)) {
            BillReminderScheduler.schedule(context.applicationContext)
        }
    }
}
