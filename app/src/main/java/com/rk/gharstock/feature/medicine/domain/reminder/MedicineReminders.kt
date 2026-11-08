package com.rk.gharstock.feature.medicine.domain.reminder

import com.rk.gharstock.feature.medicine.domain.model.Medicine

/** Keeps one alarm per medicine for its next planned dose. */
interface DoseReminderScheduler {
    /** Schedules (or replaces) the alarm for the medicine's next dose; cancels it if none is left. */
    fun schedule(medicine: Medicine)

    fun cancel(medicineId: Long)

    /** Re-creates alarms for all medicines, e.g. after reboot or a time change. */
    suspend fun rescheduleAll()
}

/** Medicine notifications that domain logic can trigger. */
interface MedicineNotifier {
    fun cancelDoseNotification(medicineId: Long)

    fun showLowPills(medicine: Medicine)
}
