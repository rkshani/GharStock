package com.rk.gharstock.core.notification.expiry

import java.time.Duration
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DelayUntilNextTest {

    @Test
    fun `before 9 AM waits until 9 AM today`() {
        val now = LocalDateTime.of(2026, 11, 8, 7, 30)
        assertEquals(Duration.ofMinutes(90), delayUntilNext(EXPIRY_CHECK_TIME, now))
    }

    @Test
    fun `at or after 9 AM waits until 9 AM tomorrow`() {
        assertEquals(Duration.ofHours(24), delayUntilNext(EXPIRY_CHECK_TIME, LocalDateTime.of(2026, 11, 8, 9, 0)))
        assertEquals(Duration.ofHours(23), delayUntilNext(EXPIRY_CHECK_TIME, LocalDateTime.of(2026, 11, 8, 10, 0)))
    }
}
