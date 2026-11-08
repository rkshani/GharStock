package com.rk.gharstock.feature.medicine.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.ConfirmDeleteDialog
import com.rk.gharstock.core.ui.component.ExpiryText
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.SectionHeader
import com.rk.gharstock.core.ui.text.formatDate
import com.rk.gharstock.core.ui.text.formatNumber
import com.rk.gharstock.core.ui.text.formatTime
import com.rk.gharstock.core.ui.theme.ExpiredRed
import com.rk.gharstock.feature.medicine.domain.model.DoseLog
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.daysOfPillsLeft
import com.rk.gharstock.feature.medicine.domain.model.isLowOnPills
import com.rk.gharstock.feature.medicine.ui.scheduleSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.floor

@Composable
fun MedicineDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isGone) { if (state.isGone) onBack() }
    MedicineDetailContent(state, viewModel::onEvent, onBack, onEdit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineDetailContent(
    state: MedicineDetailUiState,
    onEvent: (MedicineDetailEvent) -> Unit,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val medicine = state.medicine
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(medicine?.name ?: stringResource(R.string.medicine_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (medicine != null) {
                        IconButton(onClick = { onEdit(medicine.id) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { onEvent(MedicineDetailEvent.DeleteClicked) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (medicine == null) {
            LoadingState(Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
                item { MedicineInfoCard(medicine, state.memberName, state.today) }
                item { SectionHeader(stringResource(R.string.medicine_history)) }
                if (state.logs.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.medicine_history_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                items(state.logs, key = { it.id }) { DoseLogRow(it) }
            }
        }
    }

    if (state.showDeleteConfirm && medicine != null) {
        ConfirmDeleteDialog(
            itemName = medicine.name,
            onConfirm = { onEvent(MedicineDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(MedicineDetailEvent.DeleteDismissed) },
        )
    }
}

@Composable
private fun MedicineInfoCard(medicine: Medicine, memberName: String, today: LocalDate) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(memberName, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.medicine_dose_format, formatNumber(medicine.doseAmount)))
            Text(scheduleSummary(medicine.schedule), style = MaterialTheme.typography.bodyMedium)
            val low = medicine.isLowOnPills()
            Text(
                text = stringResource(
                    if (low) R.string.medicine_low_pills else R.string.medicine_pills_left,
                    formatNumber(medicine.pillsLeft),
                ),
                color = if (low) ExpiredRed else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (low) FontWeight.SemiBold else null,
            )
            medicine.daysOfPillsLeft()?.let { days ->
                Text(
                    stringResource(R.string.medicine_days_left, formatNumber(floor(days))),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ExpiryText(medicine.expiryDate, today)
            medicine.notes?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun DoseLogRow(log: DoseLog) {
    val time = Instant.ofEpochMilli(log.scheduledAt).atZone(ZoneId.systemDefault())
    val taken = log.status == DoseStatus.TAKEN
    ListItem(
        headlineContent = { Text("${formatDate(time.toLocalDate())} · ${formatTime(time.toLocalTime())}") },
        supportingContent = { Text(stringResource(if (taken) R.string.dose_taken else R.string.dose_skipped)) },
        leadingContent = {
            Icon(
                if (taken) Icons.Outlined.CheckCircle else Icons.Outlined.RemoveCircleOutline,
                contentDescription = null,
                tint = if (taken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
