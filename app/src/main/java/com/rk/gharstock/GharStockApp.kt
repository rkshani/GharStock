package com.rk.gharstock

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.rk.gharstock.core.notification.NotificationChannels
import com.rk.gharstock.core.notification.expiry.ExpiryCheckScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GharStockApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var expiryCheckScheduler: ExpiryCheckScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        expiryCheckScheduler.schedule()
    }
}
