package com.rk.gharstock.core.database

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

private const val SEPARATOR = ","

/** Dates as epoch day, times as minute of day, collections as comma-separated values. */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? = time?.let { it.hour * 60 + it.minute }

    @TypeConverter
    fun toLocalTime(minuteOfDay: Int?): LocalTime? = minuteOfDay?.let { LocalTime.of(it / 60, it % 60) }

    @TypeConverter
    fun fromTimes(times: List<LocalTime>): String =
        times.joinToString(SEPARATOR) { fromLocalTime(it).toString() }

    @TypeConverter
    fun toTimes(value: String): List<LocalTime> =
        value.splitValues().map { requireNotNull(toLocalTime(it.toInt())) }

    @TypeConverter
    fun fromDaysOfWeek(days: Set<DayOfWeek>): String =
        days.sorted().joinToString(SEPARATOR) { it.value.toString() }

    @TypeConverter
    fun toDaysOfWeek(value: String): Set<DayOfWeek> =
        value.splitValues().map { DayOfWeek.of(it.toInt()) }.toSet()

    private fun String.splitValues(): List<String> =
        if (isBlank()) emptyList() else split(SEPARATOR)
}
