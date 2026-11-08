package com.rk.gharstock.core.notification.dose

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rk.gharstock.core.di.ApplicationScope
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

/** Fires at a planned dose time: shows the dose notification and schedules the next dose. */
@AndroidEntryPoint
class DoseAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicineRepository

    @Inject lateinit var scheduler: DoseReminderScheduler

    @Inject lateinit var notifier: DoseNotifier

    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, 0)
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, 0)
        launchAsync(scope) {
            val medicine = repository.getMedicine(medicineId) ?: return@launchAsync
            if (!repository.isDoseLogged(medicineId, scheduledAt)) {
                notifier.showDose(medicine, scheduledAt)
            }
            scheduler.schedule(medicine)
        }
    }

    companion object {
        const val EXTRA_MEDICINE_ID = "medicine_id"
        const val EXTRA_SCHEDULED_AT = "scheduled_at"

        /** One pending intent per medicine (request code = medicine id), so a new dose replaces the old alarm. */
        fun pendingIntent(context: Context, medicineId: Long, scheduledAt: Long): PendingIntent {
            val intent = Intent(context, DoseAlarmReceiver::class.java)
                .putExtra(EXTRA_MEDICINE_ID, medicineId)
                .putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
            return PendingIntent.getBroadcast(
                context,
                medicineId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
