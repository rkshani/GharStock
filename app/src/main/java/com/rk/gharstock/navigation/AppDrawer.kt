package com.rk.gharstock.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rk.gharstock.R

private const val PRIVACY_POLICY_URL = "https://sites.google.com/view/ghar-khata/home"

/** Side drawer opened from the Home top bar. [onClose] is called before every action. */
@Composable
fun AppDrawerContent(isHomeSelected: Boolean, onOpenHome: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    ModalDrawerSheet {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
        )
        HorizontalDivider(Modifier.padding(bottom = 8.dp))
        DrawerItem(Icons.Outlined.Home, R.string.drawer_home, selected = isHomeSelected) {
            onClose()
            onOpenHome()
        }
        DrawerItem(Icons.Outlined.StarRate, R.string.drawer_rate_us) {
            onClose()
            context.openRateUs()
        }
        DrawerItem(Icons.Outlined.PrivacyTip, R.string.drawer_privacy_policy) {
            onClose()
            context.openUrl(PRIVACY_POLICY_URL)
        }
        DrawerItem(Icons.AutoMirrored.Outlined.ExitToApp, R.string.drawer_exit) {
            onClose()
            context.findActivity()?.finishAffinity()
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: Int, selected: Boolean = false, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(label)) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}

private fun Context.openRateUs() {
    val marketUri = Uri.parse("market://details?id=$packageName")
    if (!tryStartView(marketUri)) openUrl("https://play.google.com/store/apps/details?id=$packageName")
}

private fun Context.openUrl(url: String) {
    if (!tryStartView(Uri.parse(url))) {
        Toast.makeText(this, R.string.drawer_no_app_to_open, Toast.LENGTH_SHORT).show()
    }
}

private fun Context.tryStartView(uri: Uri): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    false
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
