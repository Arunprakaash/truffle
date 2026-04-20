package com.truffleapp.truffle

import android.app.Application
import com.truffleapp.truffle.reminders.BillReminderNotifications
import com.truffleapp.truffle.reminders.BillReminderPrefs
import com.truffleapp.truffle.reminders.BillReminderScheduler

class TruffleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BillReminderNotifications.ensureChannel(this)
        if (BillReminderPrefs.isEnabled(this)) {
            BillReminderScheduler.schedule(this)
        }
    }
}
