package com.truffleapp.truffle.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.truffleapp.truffle.MainActivity
import com.truffleapp.truffle.R
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.formatLedgerMoney
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.data.parseBillRecurrence

internal const val BILL_REMINDER_CHANNEL_ID = "bills"
internal const val BILL_REMINDER_NOTIFICATION_ID = 2001

object BillReminderNotifications {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            BILL_REMINDER_CHANNEL_ID,
            context.getString(R.string.bill_reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.bill_reminders_channel_desc)
        }
        mgr.createNotificationChannel(channel)
    }

    fun cancelSummary(context: Context) {
        NotificationManagerCompat.from(context).cancel(BILL_REMINDER_NOTIFICATION_ID)
    }

    fun showBillSummary(context: Context, bills: List<Bill>, displayCurrency: String) {
        if (bills.isEmpty()) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val dc = normalizeLedgerCurrencyCode(displayCurrency)
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val maxLines = 6
        val lines = bills.take(maxLines).map { bill ->
            val due = bill.relativeDuePhrase()
            val amt = formatLedgerMoney(bill.amount, dc, cents = true)
            "${bill.label} · $amt · $due"
        }.toMutableList()
        if (bills.size > maxLines) {
            lines.add(context.getString(R.string.bill_reminders_more, bills.size - maxLines))
        }

        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(context.getString(R.string.bill_reminders_title))
        lines.forEach { style.addLine(it) }

        val notification = NotificationCompat.Builder(context, BILL_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.bill_reminders_title))
            .setContentText(lines.first())
            .setStyle(style)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(BILL_REMINDER_NOTIFICATION_ID, notification)
    }
}

internal fun com.truffleapp.truffle.data.db.BillEntity.toBill(): Bill = Bill(
    id = id,
    label = label,
    amount = amount,
    dueDateEpoch = dueDateEpoch,
    paid = paid,
    account = account,
    recurrence = parseBillRecurrence(recurrence),
)
