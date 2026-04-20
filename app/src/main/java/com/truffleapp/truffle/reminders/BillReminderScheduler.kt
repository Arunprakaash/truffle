package com.truffleapp.truffle.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "bill_reminder_checks"

object BillReminderScheduler {

    /** Runs about once a day while reminders stay enabled. */
    fun schedule(context: Context) {
        if (!BillReminderPrefs.isEnabled(context)) {
            cancel(context)
            return
        }
        val request = PeriodicWorkRequestBuilder<BillReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        BillReminderNotifications.cancelSummary(context)
    }
}
