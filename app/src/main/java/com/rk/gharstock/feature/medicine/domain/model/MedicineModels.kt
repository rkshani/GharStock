package com.rk.gharstock.feature.medicine.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class FamilyMember(
    val id: Long = 0,
    val name: String,
    val relation: String? = null,
)

enum class ScheduleType {
    /** Same times every day, e.g. 8 AM, 2 PM, 10 PM. */
    FIXED_TIMES,

    /** Every X hours from a start time. */
    EVERY_X_HOURS,

    /** Chosen days of the week at fixed times. */
    DAYS_OF_WEEK,

    /** Fixed times every day for a number of days from the start date. */
    COURSE,
}

data class DoseSchedule(
    val type: ScheduleType,
    val times: List<LocalTime> = emptyList(),
    val startTime: LocalTime? = null,
    val intervalHours: Int? = null,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val startDate: LocalDate,
    val durationDays: Int? = null,
)

data class Medicine(
    val id: Long = 0,
    val memberId: Long,
    val name: String,
    val doseAmount: Double,
    val pillsLeft: Double,
    val expiryDate: LocalDate? = null,
    val notes: String? = null,
    val schedule: DoseSchedule,
)

data class MemberWithMedicines(
    val member: FamilyMember,
    val medicines: List<Medicine>,
)

enum class DoseStatus { TAKEN, SKIPPED }

data class DoseLog(
    val id: Long = 0,
    val medicineId: Long,
    val scheduledAt: Long,
    val status: DoseStatus,
    val loggedAt: Long,
)
