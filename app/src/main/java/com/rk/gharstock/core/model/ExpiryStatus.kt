package com.rk.gharstock.core.model

import java.time.LocalDate

/** Number of days before expiry when an item counts as "expiring soon". */
const val EXPIRY_WARNING_DAYS = 3L

enum class ExpiryStatus {
    /** No expiry date set. */
    NONE,
    SAFE,
    EXPIRING_SOON,
    EXPIRED,
}

fun expiryStatusOf(expiryDate: LocalDate?, today: LocalDate): ExpiryStatus = when {
    expiryDate == null -> ExpiryStatus.NONE
    expiryDate.isBefore(today) -> ExpiryStatus.EXPIRED
    !expiryDate.isAfter(today.plusDays(EXPIRY_WARNING_DAYS)) -> ExpiryStatus.EXPIRING_SOON
    else -> ExpiryStatus.SAFE
}
