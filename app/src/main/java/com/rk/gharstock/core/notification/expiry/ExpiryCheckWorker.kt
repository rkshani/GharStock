package com.rk.gharstock.core.notification.expiry

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rk.gharstock.core.domain.expiry.ObserveExpiryAlertsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** Daily job: notifies about expired items and items expiring within 3 days. */
@HiltWorker
class ExpiryCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val observeExpiryAlerts: ObserveExpiryAlertsUseCase,
    private val notifier: ExpiryNotifier,
    private val clock: Clock,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now(clock)
        notifier.show(observeExpiryAlerts(today).first(), today)
        return Result.success()
    }
}
