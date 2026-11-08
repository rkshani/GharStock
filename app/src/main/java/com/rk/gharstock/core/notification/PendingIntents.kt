package com.rk.gharstock.core.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.rk.gharstock.MainActivity

/** Opens (or brings back) the app when a notification is tapped. */
fun Context.openAppPendingIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}
