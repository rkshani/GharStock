package com.rk.gharstock.feature.medicine.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleRulesTest {

    private val start = LocalDate.of(2026, 11, 1)
    private val eight = LocalTime.of(8, 0)
    private val twenty = LocalTime.of(20, 0)

    private fun medicine(schedule: DoseSchedule, pillsLeft: Double, dose: Double = 1.0) =
        Medicine(memberId = 1, name = "Med", doseAmount = dose, pillsLeft = pillsLeft, schedule = schedule)

    @Test
    fun `doses per day for each schedule type`() {
        assertEquals(2.0, DoseSchedule(ScheduleType.FIXED_TIMES, times = listOf(eight, twenty), startDate = start).dosesPerDay(), 0.0)
        assertEquals(3.0, DoseSchedule(ScheduleType.EVERY_X_HOURS, startTime = eight, intervalHours = 8, startDate = start).dosesPerDay(), 0.0)
        assertEquals(
            1.0,
            DoseSchedule(
                ScheduleType.DAYS_OF_WEEK,
                times = listOf(eight, twenty),
                daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
                startDate = start,
            ).dosesPerDay(),
            0.15,
        )
        assertEquals(1.0, DoseSchedule(ScheduleType.COURSE, times = listOf(eight), durationDays = 5, startDate = start).dosesPerDay(), 0.0)
    }

    @Test
    fun `low pills when pills cover three days or fewer`() {
        val twiceDaily = DoseSchedule(ScheduleType.FIXED_TIMES, times = listOf(eight, twenty), startDate = start)

        assertTrue(medicine(twiceDaily, pillsLeft = 6.0).isLowOnPills())
        assertFalse(medicine(twiceDaily, pillsLeft = 7.0).isLowOnPills())
        assertEquals(1.5, medicine(twiceDaily, pillsLeft = 6.0, dose = 2.0).daysOfPillsLeft()!!, 0.0)
    }

    @Test
    fun `schedule without doses has no pill estimate`() {
        val empty = DoseSchedule(ScheduleType.FIXED_TIMES, startDate = start)

        assertNull(medicine(empty, pillsLeft = 1.0).daysOfPillsLeft())
        assertFalse(medicine(empty, pillsLeft = 1.0).isLowOnPills())
    }

    @Test
    fun `validation requires the fields of each type`() {
        assertTrue(DoseSchedule(ScheduleType.FIXED_TIMES, times = listOf(eight), startDate = start).isValid())
        assertFalse(DoseSchedule(ScheduleType.FIXED_TIMES, startDate = start).isValid())
        assertFalse(DoseSchedule(ScheduleType.EVERY_X_HOURS, startTime = eight, intervalHours = 0, startDate = start).isValid())
        assertFalse(DoseSchedule(ScheduleType.DAYS_OF_WEEK, times = listOf(eight), startDate = start).isValid())
        assertFalse(DoseSchedule(ScheduleType.COURSE, times = listOf(eight), startDate = start).isValid())
        assertFalse(DoseSchedule(ScheduleType.COURSE, times = listOf(eight), durationDays = 0, startDate = start).isValid())
        assertTrue(DoseSchedule(ScheduleType.COURSE, times = listOf(eight), durationDays = 7, startDate = start).isValid())
    }
}
