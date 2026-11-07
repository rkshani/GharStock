package com.rk.gharstock.feature.medicine.domain.model

private val VALID_INTERVAL_HOURS = 1..24

/** True when the schedule has every field its [ScheduleType] needs. */
fun DoseSchedule.isValid(): Boolean {
    if (durationDays != null && durationDays < 1) return false
    return when (type) {
        ScheduleType.FIXED_TIMES -> times.isNotEmpty()
        ScheduleType.EVERY_X_HOURS -> startTime != null && intervalHours in VALID_INTERVAL_HOURS
        ScheduleType.DAYS_OF_WEEK -> times.isNotEmpty() && daysOfWeek.isNotEmpty()
        ScheduleType.COURSE -> times.isNotEmpty() && durationDays != null
    }
}
