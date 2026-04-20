package com.truffleapp.truffle.reminders

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.truffleapp.truffle.data.DEFAULT_LEDGER_CURRENCY
import com.truffleapp.truffle.data.db.LedgerDatabase
import java.time.LocalDate

class BillReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (!BillReminderPrefs.isEnabled(ctx)) {
            BillReminderNotifications.cancelSummary(ctx)
            return Result.success()
        }
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            return Result.success()
        }

        val dao = LedgerDatabase.getInstance(ctx).ledgerDao()
        val displayCurrency = dao.getMeta()?.displayCurrency ?: DEFAULT_LEDGER_CURRENCY

        val today = LocalDate.now().toEpochDay()
        val windowStart = today - 14L
        val windowEnd = today + 3L

        val upcoming = dao.listBills()
            .map { it.toBill() }
            .filter { !it.paid }
            .filter { it.dueDateEpoch in windowStart..windowEnd }
            .sortedBy { it.dueDateEpoch }

        if (upcoming.isEmpty()) {
            BillReminderNotifications.cancelSummary(ctx)
            return Result.success()
        }

        BillReminderNotifications.showBillSummary(ctx, upcoming, displayCurrency)
        return Result.success()
    }
}
