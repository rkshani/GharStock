package com.rk.gharstock.core.notification.expiry

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val WORK_NAME = "daily_expiry_check"
val EXPIRY_CHECK_TIME: LocalTime = LocalTime.of(9, 0)

/** Delay from [now] until the next [time] (today if still ahead, otherwise tomorrow). */
fun delayUntilNext(time: LocalTime, now: LocalDateTime): Duration {
    val todayAt = now.toLocalDate().atTime(time)
    val next = if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
    return Duration.between(now, next)
}

class ExpiryCheckScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
) {
    /**
     * Schedules the daily 9 AM check. [replace] re-aligns the time, e.g. after the clock
     * or time zone changed; otherwise an existing schedule is kept.
     */
    fun schedule(replace: Boolean = false) {
        val delay = delayUntilNext(EXPIRY_CHECK_TIME, LocalDateTime.now(clock))
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
            .build()
        val policy = if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }
}
