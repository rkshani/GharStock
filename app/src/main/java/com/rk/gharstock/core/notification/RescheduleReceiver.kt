package com.rk.gharstock.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rk.gharstock.core.di.ApplicationScope
import com.rk.gharstock.core.notification.dose.launchAsync
import com.rk.gharstock.core.notification.expiry.ExpiryCheckScheduler
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

/**
 * Re-creates reminders when they may have been lost or shifted: after reboot, app update,
 * clock or time zone change, and when exact alarm permission changes.
 */
@AndroidEntryPoint
class RescheduleReceiver : BroadcastReceiver() {

    @Inject lateinit var doseScheduler: DoseReminderScheduler

    @Inject lateinit var expiryScheduler: ExpiryCheckScheduler

    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        val timeChanged = intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED
        expiryScheduler.schedule(replace = timeChanged)
        launchAsync(scope) { doseScheduler.rescheduleAll() }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
        )
    }
}
