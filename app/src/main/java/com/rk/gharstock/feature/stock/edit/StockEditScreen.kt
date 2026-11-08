package com.rk.gharstock.feature.stock.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.rk.gharstock.core.ui.component.CategoryDropdown
import com.rk.gharstock.core.ui.component.ConfirmDeleteDialog
import com.rk.gharstock.core.ui.component.DateField
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.LocalDatePickerDialog
import com.rk.gharstock.core.ui.component.PlaceDropdown
import com.rk.gharstock.core.ui.component.UnitDropdown

@Composable
fun StockEditScreen(
    onBack: () -> Unit,
    viewModel: StockEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onBack() }
    StockEditContent(state, viewModel::onEvent, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockEditContent(
    state: StockEditUiState,
    onEvent: (StockEditEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) R.string.stock_new_item else R.string.stock_edit_item)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { onEvent(StockEditEvent.DeleteClicked) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
        } else {
            StockForm(state, onEvent, Modifier.padding(padding))
        }
    }

    state.datePicker?.let { field ->
        LocalDatePickerDialog(
            initialDate = if (field == StockDateField.PURCHASE) state.form.purchaseDate else state.form.expiryDate,
            onConfirm = { onEvent(StockEditEvent.DatePicked(it)) },
            onDismiss = { onEvent(StockEditEvent.DatePickerDismissed) },
        )
    }
    if (state.showDeleteConfirm) {
        ConfirmDeleteDialog(
            itemName = state.form.name,
            onConfirm = { onEvent(StockEditEvent.DeleteConfirmed) },
            onDismiss = { onEvent(StockEditEvent.DeleteDismissed) },
        )
    }
}

@Composable
private fun StockForm(state: StockEditUiState, onEvent: (StockEditEvent) -> Unit, modifier: Modifier) {
    val form = state.form
    val change = { updated: StockItemForm -> onEvent(StockEditEvent.FormChanged(updated)) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = { change(form.copy(name = it)) },
            label = { Text(stringResource(R.string.field_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = form.quantity,
                onValueChange = { change(form.copy(quantity = it)) },
                label = { Text(stringResource(R.string.field_quantity)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            UnitDropdown(form.unit, { change(form.copy(unit = it)) }, Modifier.weight(1f))
        }
        CategoryDropdown(state.categories, form.category, { change(form.copy(category = it)) }, Modifier.fillMaxWidth())
        PlaceDropdown(state.places, form.place, { change(form.copy(place = it)) }, Modifier.fillMaxWidth())
        OutlinedTextField(
            value = form.price,
            onValueChange = { change(form.copy(price = it)) },
            label = { Text(stringResource(R.string.field_price)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        DateField(
            label = stringResource(R.string.field_purchase_date),
            date = form.purchaseDate,
            onPick = { onEvent(StockEditEvent.PickDate(StockDateField.PURCHASE)) },
        )
        DateField(
            label = stringResource(R.string.field_expiry_date),
            date = form.expiryDate,
            onPick = { onEvent(StockEditEvent.PickDate(StockDateField.EXPIRY)) },
            onClear = { change(form.copy(expiryDate = null)) },
        )
        state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        Button(onClick = { onEvent(StockEditEvent.Save) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_save))
        }
    }
}
