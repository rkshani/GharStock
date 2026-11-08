package com.rk.gharstock.feature.medicine.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Days ahead to search for the next dose; covers a full week for day-of-week schedules. */
private const val SEARCH_DAYS = 8L

/** True when [date] is inside the schedule's active period (start date and optional duration). */
fun DoseSchedule.isActiveOn(date: LocalDate): Boolean {
    if (date.isBefore(startDate)) return false
    val days = durationDays ?: return true
    return date.isBefore(startDate.plusDays(days.toLong()))
}

/** Planned dose times on [date], sorted. */
fun DoseSchedule.dosesOn(date: LocalDate): List<LocalTime> {
    if (!isActiveOn(date)) return emptyList()
    return when (type) {
        ScheduleType.FIXED_TIMES, ScheduleType.COURSE -> times.sorted()
        ScheduleType.DAYS_OF_WEEK -> if (date.dayOfWeek in daysOfWeek) times.sorted() else emptyList()
        ScheduleType.EVERY_X_HOURS -> intervalDosesOn(date)
    }
}

/** Doses every [DoseSchedule.intervalHours] hours, counted from the start date at the start time. */
private fun DoseSchedule.intervalDosesOn(date: LocalDate): List<LocalTime> {
    val hours = intervalHours?.takeIf { it > 0 } ?: return emptyList()
    val anchor = startDate.atTime(startTime ?: return emptyList())
    val interval = Duration.ofHours(hours.toLong())
    val dayStart = date.atStartOfDay()
    val dayEnd = dayStart.plusDays(1)
    val stepsBeforeDay = Duration.between(anchor, dayStart).toHours().coerceAtLeast(0) / hours
    var dose = anchor.plus(interval.multipliedBy(stepsBeforeDay))
    val result = mutableListOf<LocalTime>()
    while (dose.isBefore(dayEnd)) {
        if (!dose.isBefore(dayStart)) result += dose.toLocalTime()
        dose = dose.plus(interval)
    }
    return result
}

/** The first planned dose strictly after [after], or null when the schedule has ended. */
fun DoseSchedule.nextDoseAfter(after: LocalDateTime): LocalDateTime? {
    val firstDay = maxOf(after.toLocalDate(), startDate)
    return (0 until SEARCH_DAYS).asSequence()
        .map { firstDay.plusDays(it) }
        .flatMap { day -> dosesOn(day).asSequence().map { day.atTime(it) } }
        .firstOrNull { it.isAfter(after) }
}
