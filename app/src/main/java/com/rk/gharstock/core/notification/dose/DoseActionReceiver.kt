package com.rk.gharstock.core.notification.dose

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rk.gharstock.core.di.ApplicationScope
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.usecase.LogDoseUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

/** Handles the Taken / Skip buttons on a dose notification. */
@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    @Inject lateinit var logDose: LogDoseUseCase

    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val status = DoseStatus.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_STATUS) } ?: return
        val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, 0)
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, 0)
        launchAsync(scope) { logDose(medicineId, scheduledAt, status) }
    }

    companion object {
        private const val EXTRA_MEDICINE_ID = "medicine_id"
        private const val EXTRA_SCHEDULED_AT = "scheduled_at"
        private const val EXTRA_STATUS = "status"

        fun pendingIntent(context: Context, medicineId: Long, scheduledAt: Long, status: DoseStatus): PendingIntent {
            val intent = Intent(context, DoseActionReceiver::class.java)
                .putExtra(EXTRA_MEDICINE_ID, medicineId)
                .putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
                .putExtra(EXTRA_STATUS, status.name)
            // Distinct request code per medicine and action so Taken and Skip don't overwrite each other.
            val requestCode = medicineId.toInt() * DoseStatus.entries.size + status.ordinal
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
