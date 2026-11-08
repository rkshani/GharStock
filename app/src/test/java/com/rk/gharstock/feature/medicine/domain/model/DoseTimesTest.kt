package com.rk.gharstock.feature.medicine.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseTimesTest {

    // Sunday, 8 Nov 2026
    private val start = LocalDate.of(2026, 11, 8)
    private val eight = LocalTime.of(8, 0)
    private val twenty = LocalTime.of(20, 0)

    @Test
    fun `fixed times repeat daily from the start date`() {
        val schedule = DoseSchedule(ScheduleType.FIXED_TIMES, times = listOf(twenty, eight), startDate = start)

        assertTrue(schedule.dosesOn(start.minusDays(1)).isEmpty())
        assertEquals(listOf(eight, twenty), schedule.dosesOn(start.plusDays(10)))
    }

    @Test
    fun `course stops after its number of days`() {
        val schedule = DoseSchedule(ScheduleType.COURSE, times = listOf(eight), startDate = start, durationDays = 3)

        assertEquals(listOf(eight), schedule.dosesOn(start.plusDays(2)))
        assertTrue(schedule.dosesOn(start.plusDays(3)).isEmpty())
        assertNull(schedule.nextDoseAfter(start.plusDays(2).atTime(9, 0)))
    }

    @Test
    fun `days of week only on chosen days`() {
        val schedule = DoseSchedule(
            ScheduleType.DAYS_OF_WEEK,
            times = listOf(eight),
            daysOfWeek = setOf(DayOfWeek.WEDNESDAY),
            startDate = start,
        )

        assertTrue(schedule.dosesOn(start).isEmpty())
        assertEquals(LocalDateTime.of(2026, 11, 11, 8, 0), schedule.nextDoseAfter(start.atTime(7, 0)))
    }

    @Test
    fun `every x hours continues across midnight`() {
        val schedule = DoseSchedule(
            ScheduleType.EVERY_X_HOURS,
            startTime = LocalTime.of(6, 0),
            intervalHours = 8,
            startDate = start,
        )

        assertEquals(listOf(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(22, 0)), schedule.dosesOn(start))
        assertEquals(listOf(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(22, 0)), schedule.dosesOn(start.plusDays(1)))

        val fiveHourly = schedule.copy(intervalHours = 5)
        // 06, 11, 16, 21 on day one; then 02, 07, 12, 17, 22 on day two.
        assertEquals(LocalTime.of(2, 0), fiveHourly.dosesOn(start.plusDays(1)).first())
        assertEquals(5, fiveHourly.dosesOn(start.plusDays(1)).size)
    }

    @Test
    fun `next dose is strictly after the given time`() {
        val schedule = DoseSchedule(ScheduleType.FIXED_TIMES, times = listOf(eight, twenty), startDate = start)

        assertEquals(start.atTime(twenty), schedule.nextDoseAfter(start.atTime(eight)))
        assertEquals(start.plusDays(1).atTime(eight), schedule.nextDoseAfter(start.atTime(21, 0)))
        assertEquals(start.atTime(eight), schedule.nextDoseAfter(start.minusDays(5).atTime(12, 0)))
    }
}
