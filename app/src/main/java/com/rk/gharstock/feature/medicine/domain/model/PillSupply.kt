package com.rk.gharstock.feature.medicine.domain.model

private const val HOURS_PER_DAY = 24.0
private const val DAYS_PER_WEEK = 7.0

/** Alert when pills left cover this many days of doses or fewer. */
const val LOW_PILLS_DAYS = 3.0

/** Average number of doses per day for this schedule. */
fun DoseSchedule.dosesPerDay(): Double = when (type) {
    ScheduleType.FIXED_TIMES, ScheduleType.COURSE -> times.size.toDouble()
    ScheduleType.EVERY_X_HOURS -> intervalHours?.takeIf { it > 0 }?.let { HOURS_PER_DAY / it } ?: 0.0
    ScheduleType.DAYS_OF_WEEK -> times.size * daysOfWeek.size / DAYS_PER_WEEK
}

/** Days the remaining pills will last, or null when the schedule has no doses. */
fun Medicine.daysOfPillsLeft(): Double? {
    val pillsPerDay = schedule.dosesPerDay() * doseAmount
    return if (pillsPerDay > 0) pillsLeft / pillsPerDay else null
}

fun Medicine.isLowOnPills(): Boolean = daysOfPillsLeft()?.let { it <= LOW_PILLS_DAYS } ?: false
