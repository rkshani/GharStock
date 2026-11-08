package com.rk.gharstock.core.notification.dose

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.rk.gharstock.core.notification.canScheduleExactAlarms
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.nextDoseAfter
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * One AlarmManager alarm per medicine, for its next dose. Uses exact alarms when allowed,
 * otherwise falls back to an inexact alarm that may arrive a few minutes late.
 */
class AlarmDoseReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: MedicineRepository,
    private val clock: Clock,
) : DoseReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(medicine: Medicine) {
        val next = medicine.schedule.nextDoseAfter(LocalDateTime.now(clock))
        if (next == null) {
            cancel(medicine.id)
            return
        }
        val triggerAt = next.atZone(clock.zone).toInstant().toEpochMilli()
        val intent = DoseAlarmReceiver.pendingIntent(context, medicine.id, triggerAt)
        if (context.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    override fun cancel(medicineId: Long) {
        val intent = Intent(context, DoseAlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )?.let(alarmManager::cancel)
    }

    override suspend fun rescheduleAll() {
        repository.getAllMedicines().forEach(::schedule)
    }
}
