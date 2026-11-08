package com.rk.gharstock.feature.settings.more

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.rk.gharstock.R
import com.rk.gharstock.core.notification.openExactAlarmSettings
import com.rk.gharstock.core.notification.openNotificationSettings
import com.rk.gharstock.core.ui.component.SectionHeader
import com.rk.gharstock.core.ui.component.rememberExactAlarmsAllowed
import com.rk.gharstock.core.ui.component.rememberNotificationsEnabled

/** Destinations reachable from the More tab. */
enum class MoreDestination { REPORT, CATEGORIES, PLACES, MEMBERS, BACKUP, ABOUT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(onOpen: (MoreDestination) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.more_title)) }) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(stringResource(R.string.more_section_insights))
            MoreRow(Icons.Outlined.BarChart, R.string.report_title, R.string.report_summary) {
                onOpen(MoreDestination.REPORT)
            }
            SectionHeader(stringResource(R.string.more_section_manage))
            MoreRow(Icons.Outlined.Category, R.string.manage_categories, R.string.manage_categories_summary) {
                onOpen(MoreDestination.CATEGORIES)
            }
            MoreRow(Icons.Outlined.Kitchen, R.string.manage_places, R.string.manage_places_summary) {
                onOpen(MoreDestination.PLACES)
            }
            MoreRow(Icons.Outlined.People, R.string.manage_members, R.string.manage_members_summary) {
                onOpen(MoreDestination.MEMBERS)
            }
            SectionHeader(stringResource(R.string.more_section_app))
            MoreRow(Icons.Outlined.Backup, R.string.backup_title, R.string.backup_summary) {
                onOpen(MoreDestination.BACKUP)
            }
            val context = LocalContext.current
            MoreRow(
                icon = Icons.Outlined.Notifications,
                title = R.string.notifications_title,
                summary = if (rememberNotificationsEnabled()) R.string.notifications_on else R.string.notifications_off,
                onClick = context::openNotificationSettings,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MoreRow(
                    icon = Icons.Outlined.Alarm,
                    title = R.string.exact_alarms_title,
                    summary = if (rememberExactAlarmsAllowed()) R.string.notifications_on else R.string.exact_alarms_off,
                    onClick = context::openExactAlarmSettings,
                )
            }
            MoreRow(Icons.Outlined.Info, R.string.about_title, null) { onOpen(MoreDestination.ABOUT) }
        }
    }
}

@Composable
fun MoreRow(icon: ImageVector, title: Int, summary: Int?, onClick: () -> Unit) {
    MoreRow(icon, stringResource(title), summary?.let { stringResource(it) }, onClick)
}

@Composable
fun MoreRow(icon: ImageVector, title: String, summary: String?, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick, role = Role.Button),
    )
}
