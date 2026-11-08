package com.rk.gharstock.core.notification.dose

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rk.gharstock.R
import com.rk.gharstock.core.notification.NotificationChannels
import com.rk.gharstock.core.notification.areNotificationsEnabled
import com.rk.gharstock.core.notification.openAppPendingIntent
import com.rk.gharstock.core.ui.text.formatNumber
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.reminder.MedicineNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val LOW_PILLS_ID_OFFSET = 1_000_000

/** Dose reminders with Taken/Skip actions, and low pill alerts. One dose notification per medicine. */
class DoseNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : MedicineNotifier {

    private val manager = NotificationManagerCompat.from(context)

    @SuppressLint("MissingPermission") // Checked by areNotificationsEnabled().
    fun showDose(medicine: Medicine, scheduledAt: Long) {
        if (!context.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, NotificationChannels.DOSES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.dose_notification_title, medicine.name))
            .setContentText(context.getString(R.string.medicine_dose_format, formatNumber(medicine.doseAmount)))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setWhen(scheduledAt)
            .setShowWhen(true)
            .setContentIntent(context.openAppPendingIntent())
            .addAction(0, context.getString(R.string.dose_action_taken), actionIntent(medicine, scheduledAt, DoseStatus.TAKEN))
            .addAction(0, context.getString(R.string.dose_action_skip), actionIntent(medicine, scheduledAt, DoseStatus.SKIPPED))
            .build()
        manager.notify(medicine.id.toInt(), notification)
    }

    override fun cancelDoseNotification(medicineId: Long) {
        manager.cancel(medicineId.toInt())
    }

    @SuppressLint("MissingPermission") // Checked by areNotificationsEnabled().
    override fun showLowPills(medicine: Medicine) {
        if (!context.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, NotificationChannels.LOW_PILLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.low_pills_notification_title, medicine.name))
            .setContentText(context.getString(R.string.medicine_pills_left, formatNumber(medicine.pillsLeft)))
            .setContentIntent(context.openAppPendingIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(LOW_PILLS_ID_OFFSET + medicine.id.toInt(), notification)
    }

    private fun actionIntent(medicine: Medicine, scheduledAt: Long, status: DoseStatus) =
        DoseActionReceiver.pendingIntent(context, medicine.id, scheduledAt, status)
}
