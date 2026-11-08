package com.rk.gharstock.feature.medicine.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.text.formatDate
import com.rk.gharstock.core.ui.text.formatTime
import com.rk.gharstock.feature.medicine.domain.model.DoseSchedule
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@get:StringRes
val ScheduleType.labelRes: Int
    get() = when (this) {
        ScheduleType.FIXED_TIMES -> R.string.schedule_fixed_times
        ScheduleType.EVERY_X_HOURS -> R.string.schedule_every_x_hours
        ScheduleType.DAYS_OF_WEEK -> R.string.schedule_days_of_week
        ScheduleType.COURSE -> R.string.schedule_course
    }

fun DayOfWeek.shortName(): String = getDisplayName(TextStyle.SHORT, Locale.getDefault())

/** One line describing when doses are due, e.g. "Daily at 8:00 AM, 8:00 PM". */
@Composable
fun scheduleSummary(schedule: DoseSchedule): String {
    val times = schedule.times.joinToString(", ") { formatTime(it) }
    return when (schedule.type) {
        ScheduleType.FIXED_TIMES -> stringResource(R.string.schedule_summary_daily, times)
        ScheduleType.EVERY_X_HOURS -> stringResource(
            R.string.schedule_summary_interval,
            schedule.intervalHours ?: 0,
            schedule.startTime?.let { formatTime(it) }.orEmpty(),
        )
        ScheduleType.DAYS_OF_WEEK -> stringResource(
            R.string.schedule_summary_days,
            schedule.daysOfWeek.sorted().joinToString(", ") { it.shortName() },
            times,
        )
        ScheduleType.COURSE -> stringResource(
            R.string.schedule_summary_course,
            stringResource(R.string.schedule_summary_daily, times),
            schedule.durationDays ?: 0,
            formatDate(schedule.startDate),
        )
    }
}
