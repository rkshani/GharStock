package com.rk.gharstock

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.rk.gharstock.core.di.ApplicationScope
import com.rk.gharstock.core.notification.NotificationChannels
import com.rk.gharstock.core.notification.expiry.ExpiryCheckScheduler
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class GharStockApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var expiryCheckScheduler: ExpiryCheckScheduler

    @Inject lateinit var doseReminderScheduler: DoseReminderScheduler

    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        expiryCheckScheduler.schedule()
        // Alarms are lost when the app is force-stopped or data is restored, so refresh them on start.
        applicationScope.launch { doseReminderScheduler.rescheduleAll() }
    }
}
