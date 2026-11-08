package com.rk.gharstock.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.rk.gharstock.R

object NotificationChannels {
    const val EXPIRY = "expiry_reminders"
    const val DOSES = "medicine_doses"
    const val LOW_PILLS = "low_pills"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                channel(context, EXPIRY, R.string.channel_expiry_name, R.string.channel_expiry_description, NotificationManager.IMPORTANCE_DEFAULT),
                channel(context, DOSES, R.string.channel_doses_name, R.string.channel_doses_description, NotificationManager.IMPORTANCE_HIGH),
                channel(context, LOW_PILLS, R.string.channel_low_pills_name, R.string.channel_low_pills_description, NotificationManager.IMPORTANCE_DEFAULT),
            ),
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun channel(context: Context, id: String, name: Int, description: Int, importance: Int) =
        NotificationChannel(id, context.getString(name), importance).apply {
            this.description = context.getString(description)
        }
}

fun Context.areNotificationsEnabled(): Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled()

/** Opens the system notification settings for this app. */
fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.fromParts("package", packageName, null))
    }
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
