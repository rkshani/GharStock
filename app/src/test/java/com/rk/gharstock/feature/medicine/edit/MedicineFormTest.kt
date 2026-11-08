package com.rk.gharstock.feature.medicine.edit

import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import com.rk.gharstock.feature.medicine.domain.model.isValid
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicineFormTest {

    private val form = MedicineForm(
        memberId = 1,
        name = "Panadol",
        doseAmount = "1.5",
        pillsLeft = "20",
        times = listOf(LocalTime.of(8, 0)),
        startTime = LocalTime.of(6, 0),
        intervalHours = "6",
        daysOfWeek = setOf(DayOfWeek.MONDAY),
        startDate = LocalDate.of(2026, 11, 8),
        durationDays = "5",
    )

    @Test
    fun `fixed times keeps only times`() {
        val schedule = form.copy(type = ScheduleType.FIXED_TIMES).toMedicine(0).schedule

        assertEquals(listOf(LocalTime.of(8, 0)), schedule.times)
        assertNull(schedule.startTime)
        assertNull(schedule.intervalHours)
        assertTrue(schedule.daysOfWeek.isEmpty())
        assertNull(schedule.durationDays)
    }

    @Test
    fun `every x hours keeps start time and interval only`() {
        val schedule = form.copy(type = ScheduleType.EVERY_X_HOURS).toMedicine(0).schedule

        assertTrue(schedule.times.isEmpty())
        assertEquals(LocalTime.of(6, 0), schedule.startTime)
        assertEquals(6, schedule.intervalHours)
    }

    @Test
    fun `course needs a valid number of days`() {
        assertTrue(form.copy(type = ScheduleType.COURSE).toMedicine(0).schedule.isValid())
        assertFalse(form.copy(type = ScheduleType.COURSE, durationDays = "").toMedicine(0).schedule.isValid())
    }

    @Test
    fun `numbers are parsed and invalid input becomes NaN`() {
        val medicine = form.copy(doseAmount = "abc").toMedicine(7)

        assertEquals(7L, medicine.id)
        assertTrue(medicine.doseAmount.isNaN())
        assertEquals(20.0, medicine.pillsLeft, 0.0)
    }
}
