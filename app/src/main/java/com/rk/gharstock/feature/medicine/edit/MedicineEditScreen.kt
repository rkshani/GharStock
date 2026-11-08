package com.rk.gharstock.feature.medicine.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.DateField
import com.rk.gharstock.core.ui.component.DropdownField
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.LocalDatePickerDialog
import com.rk.gharstock.core.ui.component.LocalTimePickerDialog
import com.rk.gharstock.core.ui.text.formatTime
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import com.rk.gharstock.feature.medicine.ui.labelRes
import com.rk.gharstock.feature.medicine.ui.shortName
import java.time.DayOfWeek

@Composable
fun MedicineEditScreen(
    onBack: () -> Unit,
    viewModel: MedicineEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onBack() }
    MedicineEditContent(state, viewModel::onEvent, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineEditContent(
    state: MedicineEditUiState,
    onEvent: (MedicineEditEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) R.string.medicine_new else R.string.medicine_edit)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
        } else {
            MedicineForm(state, onEvent, Modifier.padding(padding))
        }
    }
    MedicinePickers(state, onEvent)
}

@Composable
private fun MedicinePickers(state: MedicineEditUiState, onEvent: (MedicineEditEvent) -> Unit) {
    val form = state.form
    when (state.picker) {
        MedicinePicker.ADD_TIME, MedicinePicker.START_TIME -> LocalTimePickerDialog(
            initialTime = if (state.picker == MedicinePicker.START_TIME) form.startTime else form.times.lastOrNull(),
            onConfirm = { onEvent(MedicineEditEvent.TimePicked(it)) },
            onDismiss = { onEvent(MedicineEditEvent.PickerDismissed) },
        )
        MedicinePicker.START_DATE, MedicinePicker.EXPIRY_DATE -> LocalDatePickerDialog(
            initialDate = if (state.picker == MedicinePicker.START_DATE) form.startDate else form.expiryDate,
            onConfirm = { onEvent(MedicineEditEvent.DatePicked(it)) },
            onDismiss = { onEvent(MedicineEditEvent.PickerDismissed) },
        )
        null -> Unit
    }
}

@Composable
private fun MedicineForm(state: MedicineEditUiState, onEvent: (MedicineEditEvent) -> Unit, modifier: Modifier) {
    val form = state.form
    val change = { updated: MedicineForm -> onEvent(MedicineEditEvent.FormChanged(updated)) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DropdownField(
            label = stringResource(R.string.field_member),
            options = state.members,
            selectedText = state.members.firstOrNull { it.id == form.memberId }?.name.orEmpty(),
            optionText = { it.name },
            onSelect = { change(form.copy(memberId = it.id)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.name,
            onValueChange = { change(form.copy(name = it)) },
            label = { Text(stringResource(R.string.field_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(form.doseAmount, R.string.field_dose_amount, Modifier.weight(1f)) {
                change(form.copy(doseAmount = it))
            }
            NumberField(form.pillsLeft, R.string.field_pills_left, Modifier.weight(1f)) {
                change(form.copy(pillsLeft = it))
            }
        }
        DateField(
            label = stringResource(R.string.field_expiry_date),
            date = form.expiryDate,
            onPick = { onEvent(MedicineEditEvent.OpenPicker(MedicinePicker.EXPIRY_DATE)) },
            onClear = { change(form.copy(expiryDate = null)) },
        )
        OutlinedTextField(
            value = form.notes,
            onValueChange = { change(form.copy(notes = it)) },
            label = { Text(stringResource(R.string.field_notes)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        ScheduleSection(form, onEvent, change)
        state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        Button(onClick = { onEvent(MedicineEditEvent.Save) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_save))
        }
    }
}

@Composable
private fun ScheduleSection(
    form: MedicineForm,
    onEvent: (MedicineEditEvent) -> Unit,
    change: (MedicineForm) -> Unit,
) {
    DropdownField(
        label = stringResource(R.string.field_schedule_type),
        options = ScheduleType.entries,
        selectedText = stringResource(form.type.labelRes),
        optionText = { stringResource(it.labelRes) },
        onSelect = { change(form.copy(type = it)) },
        modifier = Modifier.fillMaxWidth(),
    )
    if (form.type == ScheduleType.EVERY_X_HOURS) {
        NumberField(form.intervalHours, R.string.field_interval_hours, Modifier.fillMaxWidth(), KeyboardType.Number) {
            change(form.copy(intervalHours = it))
        }
        TimeButton(
            label = stringResource(R.string.field_start_time),
            value = form.startTime?.let { formatTime(it) },
            onClick = { onEvent(MedicineEditEvent.OpenPicker(MedicinePicker.START_TIME)) },
        )
    } else {
        if (form.type == ScheduleType.DAYS_OF_WEEK) DaysSelector(form.daysOfWeek, onEvent)
        TimesEditor(form, onEvent)
    }
    DateField(
        label = stringResource(R.string.field_start_date),
        date = form.startDate,
        onPick = { onEvent(MedicineEditEvent.OpenPicker(MedicinePicker.START_DATE)) },
    )
    if (form.type == ScheduleType.COURSE) {
        NumberField(form.durationDays, R.string.field_duration_days, Modifier.fillMaxWidth(), KeyboardType.Number) {
            change(form.copy(durationDays = it))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaysSelector(selected: Set<DayOfWeek>, onEvent: (MedicineEditEvent) -> Unit) {
    Text(stringResource(R.string.field_days), style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DayOfWeek.entries.forEach { day ->
            FilterChip(
                selected = day in selected,
                onClick = { onEvent(MedicineEditEvent.DayToggled(day)) },
                label = { Text(day.shortName()) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimesEditor(form: MedicineForm, onEvent: (MedicineEditEvent) -> Unit) {
    Text(stringResource(R.string.field_times), style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        form.times.forEach { time ->
            val label = formatTime(time)
            InputChip(
                selected = false,
                onClick = { onEvent(MedicineEditEvent.RemoveTime(time)) },
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_remove_time, label),
                    )
                },
            )
        }
        AssistChip(
            onClick = { onEvent(MedicineEditEvent.OpenPicker(MedicinePicker.ADD_TIME)) },
            label = { Text(stringResource(R.string.action_add_time)) },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        )
    }
}

@Composable
private fun TimeButton(label: String, value: String?, onClick: () -> Unit) {
    OutlinedTextField(
        value = value.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Outlined.Schedule, contentDescription = stringResource(R.string.action_pick_time))
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun NumberField(
    value: String,
    label: Int,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
    )
}
