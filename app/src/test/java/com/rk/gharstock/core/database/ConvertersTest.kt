package com.rk.gharstock.core.database

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `local date round trips through epoch day`() {
        val date = LocalDate.of(2026, 11, 8)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
        assertNull(converters.fromLocalDate(null))
    }

    @Test
    fun `local time is stored as minute of day`() {
        assertEquals(14 * 60 + 30, converters.fromLocalTime(LocalTime.of(14, 30)))
        assertEquals(LocalTime.of(22, 0), converters.toLocalTime(22 * 60))
    }

    @Test
    fun `time list round trips including empty list`() {
        val times = listOf(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(22, 15))
        assertEquals("480,840,1335", converters.fromTimes(times))
        assertEquals(times, converters.toTimes(converters.fromTimes(times)))
        assertEquals(emptyList<LocalTime>(), converters.toTimes(""))
    }

    @Test
    fun `days of week round trip in sorted order`() {
        val days = setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY)
        assertEquals("1,5", converters.fromDaysOfWeek(days))
        assertEquals(days, converters.toDaysOfWeek("1,5"))
        assertEquals(emptySet<DayOfWeek>(), converters.toDaysOfWeek(""))
    }
}
