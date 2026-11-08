package com.rk.gharstock.feature.medicine.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.notification.openExactAlarmSettings
import com.rk.gharstock.core.ui.component.EmptyState
import com.rk.gharstock.core.ui.component.ExpiryText
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.SectionHeader
import com.rk.gharstock.core.ui.component.rememberExactAlarmsAllowed
import com.rk.gharstock.core.ui.text.formatNumber
import com.rk.gharstock.core.ui.theme.ExpiredRed
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.isLowOnPills
import com.rk.gharstock.feature.medicine.ui.scheduleSummary
import java.time.LocalDate

@Composable
fun MedicineListScreen(
    onAddMedicine: () -> Unit,
    onOpenMedicine: (Long) -> Unit,
    onManageMembers: () -> Unit,
    viewModel: MedicineListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MedicineListContent(state, onAddMedicine, onOpenMedicine, onManageMembers)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineListContent(
    state: MedicineListUiState,
    onAddMedicine: () -> Unit,
    onOpenMedicine: (Long) -> Unit,
    onManageMembers: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.medicine_title)) }) },
        floatingActionButton = {
            if (state.hasMembers) {
                ExtendedFloatingActionButton(
                    onClick = onAddMedicine,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.medicine_add)) },
                )
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingState(modifier)
            !state.hasMembers -> EmptyState(
                icon = Icons.Outlined.People,
                title = stringResource(R.string.medicine_no_members_title),
                message = stringResource(R.string.medicine_no_members_message),
                modifier = modifier,
                actionLabel = stringResource(R.string.member_add),
                onAction = onManageMembers,
            )
            !state.hasMedicines -> EmptyState(
                icon = Icons.Outlined.Medication,
                title = stringResource(R.string.medicine_empty_title),
                message = stringResource(R.string.medicine_empty_message),
                modifier = modifier,
            )
            else -> LazyColumn(modifier, contentPadding = PaddingValues(bottom = 88.dp)) {
                item(key = "exact_alarm_banner") { ExactAlarmBanner() }
                state.groups.forEach { group ->
                    item(key = "member_${group.member.id}") {
                        SectionHeader(listOfNotNull(group.member.name, group.member.relation).joinToString(" · "))
                    }
                    if (group.medicines.isEmpty()) {
                        item(key = "empty_${group.member.id}") {
                            Text(
                                stringResource(R.string.medicine_member_none),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                    items(group.medicines, key = { it.id }) { medicine ->
                        MedicineRow(medicine, state.today) { onOpenMedicine(medicine.id) }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/** Shown on Android 12+ when exact alarms are not allowed, so reminders could be late. */
@Composable
private fun ExactAlarmBanner() {
    if (rememberExactAlarmsAllowed()) return
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.exact_alarms_banner),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = context::openExactAlarmSettings) { Text(stringResource(R.string.action_allow)) }
        }
    }
}

@Composable
private fun MedicineRow(medicine: Medicine, today: LocalDate, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(medicine.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            scheduleSummary(medicine.schedule),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val low = medicine.isLowOnPills()
        Text(
            text = stringResource(
                if (low) R.string.medicine_low_pills else R.string.medicine_pills_left,
                formatNumber(medicine.pillsLeft),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = if (low) ExpiredRed else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (low) FontWeight.SemiBold else null,
        )
        ExpiryText(medicine.expiryDate, today)
    }
}
