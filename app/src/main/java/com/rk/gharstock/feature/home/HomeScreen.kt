package com.rk.gharstock.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.domain.expiry.ExpiryAlert
import com.rk.gharstock.core.domain.expiry.ExpirySource
import com.rk.gharstock.core.ui.component.ExpiryText
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.SectionHeader
import com.rk.gharstock.core.ui.component.UserMessageEffect
import com.rk.gharstock.core.ui.text.formatNumber
import com.rk.gharstock.core.ui.text.formatTime
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.usecase.TodayDose
import java.time.LocalDate

@Composable
fun HomeScreen(
    onOpenStock: () -> Unit,
    onOpenShopping: () -> Unit,
    onOpenStockItem: (Long) -> Unit,
    onOpenMedicine: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state, viewModel::onEvent, onOpenStock, onOpenShopping, onOpenStockItem, onOpenMedicine)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onOpenStock: () -> Unit,
    onOpenShopping: () -> Unit,
    onOpenStockItem: (Long) -> Unit,
    onOpenMedicine: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(state.message, snackbarHostState) { onEvent(HomeEvent.MessageShown) }
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "counts") {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CountCard(Icons.Outlined.Inventory2, state.stockCount, R.string.home_items_in_stock, onOpenStock, Modifier.weight(1f))
                    CountCard(
                        Icons.Outlined.ShoppingCart,
                        state.pendingShoppingCount,
                        R.string.home_shopping_pending,
                        onOpenShopping,
                        Modifier.weight(1f),
                    )
                }
            }
            dosesSection(state.todayDoses, onEvent, onOpenMedicine)
            expiringSection(state.expiring, state.today, onOpenStockItem, onOpenMedicine)
        }
    }
}

@Composable
private fun CountCard(icon: ImageVector, count: Int, label: Int, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun LazyListScope.dosesSection(
    doses: List<TodayDose>,
    onEvent: (HomeEvent) -> Unit,
    onOpenMedicine: (Long) -> Unit,
) {
    item(key = "doses_header") { SectionHeader(stringResource(R.string.home_today_medicines)) }
    if (doses.isEmpty()) {
        item(key = "doses_empty") { EmptyLine(R.string.home_no_doses) }
    }
    items(doses, key = { "dose_${it.medicine.id}_${it.scheduledAt}" }) { dose ->
        DoseRow(dose, onEvent) { onOpenMedicine(dose.medicine.id) }
    }
}

@Composable
private fun DoseRow(dose: TodayDose, onEvent: (HomeEvent) -> Unit, onClick: () -> Unit) {
    val time = formatTime(dose.time)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(time, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(88.dp))
        Column(Modifier.weight(1f)) {
            Text(dose.medicine.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.home_dose_subtitle, dose.memberName, formatNumber(dose.medicine.doseAmount)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when (dose.status) {
            DoseStatus.TAKEN -> StatusIcon(Icons.Outlined.CheckCircle, R.string.dose_taken, MaterialTheme.colorScheme.primary)
            DoseStatus.SKIPPED -> StatusIcon(
                Icons.Outlined.RemoveCircleOutline,
                R.string.dose_skipped,
                MaterialTheme.colorScheme.onSurfaceVariant,
            )
            null -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val takeLabel = stringResource(R.string.home_take_dose, dose.medicine.name, time)
                val skipLabel = stringResource(R.string.home_skip_dose, dose.medicine.name, time)
                FilledTonalButton(
                    onClick = { onEvent(HomeEvent.DoseTaken(dose)) },
                    modifier = Modifier.semantics { contentDescription = takeLabel },
                ) { Text(stringResource(R.string.dose_action_taken)) }
                OutlinedButton(
                    onClick = { onEvent(HomeEvent.DoseSkipped(dose)) },
                    modifier = Modifier.semantics { contentDescription = skipLabel },
                ) { Text(stringResource(R.string.dose_action_skip)) }
            }
        }
    }
}

@Composable
private fun StatusIcon(icon: ImageVector, label: Int, tint: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(stringResource(label), color = tint, style = MaterialTheme.typography.labelLarge)
    }
}

private fun LazyListScope.expiringSection(
    alerts: List<ExpiryAlert>,
    today: LocalDate,
    onOpenStockItem: (Long) -> Unit,
    onOpenMedicine: (Long) -> Unit,
) {
    item(key = "expiring_header") { SectionHeader(stringResource(R.string.home_expiring_soon)) }
    if (alerts.isEmpty()) {
        item(key = "expiring_empty") { EmptyLine(R.string.home_nothing_expiring) }
    }
    items(alerts, key = { "${it.source}_${it.id}" }) { alert ->
        val isMedicine = alert.source == ExpirySource.MEDICINE
        ListItem(
            headlineContent = { Text(alert.name) },
            supportingContent = { ExpiryText(alert.expiryDate, today) },
            leadingContent = {
                Icon(
                    if (isMedicine) Icons.Outlined.Medication else Icons.Outlined.Inventory2,
                    contentDescription = if (isMedicine) stringResource(R.string.home_medicine_label) else null,
                )
            },
            modifier = Modifier.clickable(role = Role.Button) {
                if (isMedicine) onOpenMedicine(alert.id) else onOpenStockItem(alert.id)
            },
        )
    }
}

@Composable
private fun EmptyLine(text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
