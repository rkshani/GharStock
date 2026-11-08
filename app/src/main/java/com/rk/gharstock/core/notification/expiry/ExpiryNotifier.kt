package com.rk.gharstock.core.notification.expiry

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rk.gharstock.R
import com.rk.gharstock.core.domain.expiry.ExpiryAlert
import com.rk.gharstock.core.model.ExpiryStatus
import com.rk.gharstock.core.notification.NotificationChannels
import com.rk.gharstock.core.notification.areNotificationsEnabled
import com.rk.gharstock.core.notification.openAppPendingIntent
import com.rk.gharstock.core.ui.text.formatDate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject

private const val EXPIRY_NOTIFICATION_ID = 1001
private const val MAX_LINES = 6

/** Shows one grouped notification listing expired and soon-to-expire items. */
class ExpiryNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    @SuppressLint("MissingPermission") // Checked by areNotificationsEnabled().
    fun show(alerts: List<ExpiryAlert>, today: LocalDate) {
        if (alerts.isEmpty() || !context.areNotificationsEnabled()) return
        val lines = alerts.map { it.line(today) }
        val title = context.resources.getQuantityString(R.plurals.expiry_notification_title, alerts.size, alerts.size)
        val style = NotificationCompat.InboxStyle().setBigContentTitle(title)
        lines.take(MAX_LINES).forEach(style::addLine)

        val notification = NotificationCompat.Builder(context, NotificationChannels.EXPIRY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(lines.joinToString(", "))
            .setStyle(style)
            .setContentIntent(context.openAppPendingIntent())
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(EXPIRY_NOTIFICATION_ID, notification)
    }

    private fun ExpiryAlert.line(today: LocalDate): String = when {
        status == ExpiryStatus.EXPIRED -> context.getString(R.string.expiry_line_expired, name, formatDate(expiryDate))
        expiryDate == today -> context.getString(R.string.expiry_line_today, name)
        else -> context.getString(R.string.expiry_line_soon, name, formatDate(expiryDate))
    }
}
