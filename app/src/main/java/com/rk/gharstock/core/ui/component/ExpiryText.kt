package com.rk.gharstock.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.rk.gharstock.R
import com.rk.gharstock.core.model.ExpiryStatus
import com.rk.gharstock.core.model.expiryStatusOf
import com.rk.gharstock.core.ui.text.formatDate
import com.rk.gharstock.core.ui.theme.ExpiredRed
import com.rk.gharstock.core.ui.theme.ExpiringOrange
import java.time.LocalDate

@Composable
fun expiryColor(status: ExpiryStatus): Color = when (status) {
    ExpiryStatus.EXPIRED -> ExpiredRed
    ExpiryStatus.EXPIRING_SOON -> ExpiringOrange
    ExpiryStatus.SAFE, ExpiryStatus.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** "Expires 12 Nov 2026" in red (expired), orange (within 3 days) or normal color. */
@Composable
fun ExpiryText(expiryDate: LocalDate?, today: LocalDate, modifier: Modifier = Modifier) {
    if (expiryDate == null) return
    val status = expiryStatusOf(expiryDate, today)
    val textRes = if (status == ExpiryStatus.EXPIRED) R.string.stock_expired_on else R.string.stock_expires_on
    Text(
        text = stringResource(textRes, formatDate(expiryDate)),
        style = MaterialTheme.typography.bodySmall,
        color = expiryColor(status),
        fontWeight = if (status == ExpiryStatus.SAFE) null else FontWeight.SemiBold,
        modifier = modifier,
    )
}
