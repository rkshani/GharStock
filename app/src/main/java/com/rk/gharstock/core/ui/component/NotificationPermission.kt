package com.rk.gharstock.core.ui.component

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.rk.gharstock.core.notification.areNotificationsEnabled
import com.rk.gharstock.core.notification.canScheduleExactAlarms

/** Asks for the Android 13+ notification permission once, when [shouldAsk] becomes true. */
@Composable
fun NotificationPermissionRequest(shouldAsk: Boolean, onAsked: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(shouldAsk) {
        if (!shouldAsk) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        onAsked()
    }
}

/** Whether notifications are enabled, refreshed each time the screen resumes. */
@Composable
fun rememberNotificationsEnabled(): Boolean = rememberOnResume { it.areNotificationsEnabled() }

/** Whether exact alarms are allowed, refreshed each time the screen resumes. */
@Composable
fun rememberExactAlarmsAllowed(): Boolean = rememberOnResume { it.canScheduleExactAlarms() }

@Composable
private fun rememberOnResume(read: (Context) -> Boolean): Boolean {
    val context = LocalContext.current
    var value by remember { mutableStateOf(read(context)) }
    LifecycleResumeEffect(Unit) {
        value = read(context)
        onPauseOrDispose { }
    }
    return value
}
