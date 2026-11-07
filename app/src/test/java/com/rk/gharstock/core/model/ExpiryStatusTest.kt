package com.rk.gharstock.core.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpiryStatusTest {

    private val today = LocalDate.of(2026, 11, 8)

    @Test
    fun `no expiry date gives none`() {
        assertEquals(ExpiryStatus.NONE, expiryStatusOf(null, today))
    }

    @Test
    fun `past date is expired`() {
        assertEquals(ExpiryStatus.EXPIRED, expiryStatusOf(today.minusDays(1), today))
    }

    @Test
    fun `today up to three days ahead is expiring soon`() {
        assertEquals(ExpiryStatus.EXPIRING_SOON, expiryStatusOf(today, today))
        assertEquals(ExpiryStatus.EXPIRING_SOON, expiryStatusOf(today.plusDays(3), today))
    }

    @Test
    fun `four or more days ahead is safe`() {
        assertEquals(ExpiryStatus.SAFE, expiryStatusOf(today.plusDays(4), today))
    }
}
