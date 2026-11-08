package com.rk.gharstock.core.ui.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rk.gharstock.R
import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.ValidationException
import com.rk.gharstock.core.model.MeasureUnit
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@get:StringRes
val MeasureUnit.labelRes: Int
    get() = when (this) {
        MeasureUnit.KG -> R.string.unit_kg
        MeasureUnit.G -> R.string.unit_g
        MeasureUnit.L -> R.string.unit_l
        MeasureUnit.ML -> R.string.unit_ml
        MeasureUnit.PCS -> R.string.unit_pcs
        MeasureUnit.PACK -> R.string.unit_pack
        MeasureUnit.DOZEN -> R.string.unit_dozen
        MeasureUnit.BOX -> R.string.unit_box
    }

@get:StringRes
val ValidationError.messageRes: Int
    get() = when (this) {
        ValidationError.BLANK_NAME -> R.string.error_blank_name
        ValidationError.DUPLICATE_NAME -> R.string.error_duplicate_name
        ValidationError.INVALID_QUANTITY -> R.string.error_invalid_quantity
        ValidationError.INVALID_PRICE -> R.string.error_invalid_price
        ValidationError.INVALID_DOSE -> R.string.error_invalid_dose
        ValidationError.INVALID_SCHEDULE -> R.string.error_invalid_schedule
        ValidationError.MISSING_MEMBER -> R.string.error_missing_member
    }

/** 2.0 -> "2", 1.5 -> "1.5", 1250.75 -> "1,250.75" */
fun formatNumber(value: Double): String = DecimalFormat("#,##0.##").format(value)

/** Number without grouping, for editable text fields. */
fun formatEditableNumber(value: Double): String = DecimalFormat("0.##").format(value)

@Composable
fun quantityText(quantity: Double, unit: MeasureUnit): String =
    stringResource(R.string.quantity_unit_format, formatNumber(quantity), stringResource(unit.labelRes))

@Composable
fun priceText(price: Double): String = stringResource(R.string.price_format, formatNumber(price))

/** Message for a failed use case result; non-validation errors are unexpected and rethrown. */
@StringRes
fun Throwable.validationMessageRes(): Int =
    (this as? ValidationException)?.error?.messageRes ?: throw this

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** e.g. "8 Nov 2026", following the device locale. */
fun formatDate(date: LocalDate): String = dateFormatter.format(date)

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/** e.g. "8:00 AM", following the device locale. */
fun formatTime(time: LocalTime): String = timeFormatter.format(time)
