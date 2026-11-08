package com.rk.gharstock.core.util

/** Parses user input like "1.5" or "1,5". Returns null when not a number. */
fun String.toDecimalOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Blank input means "no value"; otherwise parses, or returns NaN for invalid input. */
fun String.toOptionalDecimal(): Double? = if (isBlank()) null else toDecimalOrNull() ?: Double.NaN
