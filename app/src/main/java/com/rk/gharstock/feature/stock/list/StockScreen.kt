package com.rk.gharstock.feature.stock.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.ConfirmDeleteDialog
import com.rk.gharstock.core.ui.component.EmptyState
import com.rk.gharstock.core.ui.component.ExpiryText
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.OverflowMenu
import com.rk.gharstock.core.ui.component.OverflowMenuItem
import com.rk.gharstock.core.ui.component.UserMessageEffect
import com.rk.gharstock.core.ui.text.priceText
import com.rk.gharstock.core.ui.text.quantityText
import com.rk.gharstock.feature.stock.domain.model.StockItem
import com.rk.gharstock.feature.stock.domain.model.StockSort
import java.time.LocalDate

@Composable
fun StockScreen(
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
    viewModel: StockViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StockContent(state, viewModel::onEvent, onAddItem, onEditItem)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockContent(
    state: StockUiState,
    onEvent: (StockEvent) -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(state.message, snackbarHostState) { onEvent(StockEvent.MessageShown) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.stock_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItem,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.stock_add_item)) },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingState(modifier)
            !state.hasAnyItems -> EmptyState(
                icon = Icons.Outlined.Inventory2,
                title = stringResource(R.string.stock_empty_title),
                message = stringResource(R.string.stock_empty_message),
                modifier = modifier,
            )
            else -> Column(modifier) {
                StockFilters(state, onEvent)
                if (state.items.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.stock_no_results_title),
                        message = stringResource(R.string.stock_no_results_message),
                        actionLabel = stringResource(R.string.stock_clear_filters),
                        onAction = { onEvent(StockEvent.ClearFilters) },
                    )
                } else {
                    StockList(state, onEvent, onEditItem)
                }
            }
        }
    }

    state.deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            itemName = target.name,
            onConfirm = { onEvent(StockEvent.DeleteConfirmed) },
            onDismiss = { onEvent(StockEvent.DeleteDismissed) },
        )
    }
    state.addToList?.let { dialog ->
        AddToShoppingListDialog(
            dialog = dialog,
            lists = state.shoppingLists,
            onChange = { onEvent(StockEvent.AddToListChanged(it)) },
            onConfirm = { onEvent(StockEvent.AddToListConfirmed) },
            onDismiss = { onEvent(StockEvent.AddToListDismissed) },
        )
    }
}

@Composable
private fun StockFilters(state: StockUiState, onEvent: (StockEvent) -> Unit) {
    val all = stringResource(R.string.stock_filter_all)
    OutlinedTextField(
        value = state.filter.query,
        onValueChange = { onEvent(StockEvent.QueryChanged(it)) },
        placeholder = { Text(stringResource(R.string.stock_search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (state.filter.query.isNotEmpty()) {
                IconButton(onClick = { onEvent(StockEvent.QueryChanged("")) }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val category = state.categories.firstOrNull { it.id == state.filter.categoryId }
        FilterMenuChip(
            label = stringResource(R.string.stock_filter_category, category?.name ?: all),
            selected = category != null,
            options = listOf(null to all) + state.categories.map { it.id to it.name },
            onSelect = { onEvent(StockEvent.CategoryFilterChanged(it)) },
        )
        val place = state.places.firstOrNull { it.id == state.filter.placeId }
        FilterMenuChip(
            label = stringResource(R.string.stock_filter_place, place?.name ?: all),
            selected = place != null,
            options = listOf(null to all) + state.places.map { it.id to it.name },
            onSelect = { onEvent(StockEvent.PlaceFilterChanged(it)) },
        )
        FilterChip(
            selected = state.filter.sort == StockSort.EXPIRY,
            onClick = { onEvent(StockEvent.SortToggled) },
            label = {
                Text(
                    stringResource(
                        if (state.filter.sort == StockSort.EXPIRY) R.string.stock_sort_expiry else R.string.stock_sort_name,
                    ),
                )
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
        )
    }
}

@Composable
private fun FilterMenuChip(
    label: String,
    selected: Boolean,
    options: List<Pair<Long?, String>>,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    },
                )
            }
        }
    }
}

@Composable
private fun StockList(state: StockUiState, onEvent: (StockEvent) -> Unit, onEditItem: (Long) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
        items(state.items, key = { it.id }) { item ->
            StockItemRow(
                item = item,
                today = state.today,
                onClick = { onEditItem(item.id) },
                onAddToList = { onEvent(StockEvent.AddToListClicked(item)) },
                onDelete = { onEvent(StockEvent.DeleteClicked(item)) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun StockItemRow(
    item: StockItem,
    today: LocalDate,
    onClick: () -> Unit,
    onAddToList: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = listOfNotNull(
                    quantityText(item.quantity, item.unit),
                    item.category?.name,
                    item.place?.name,
                    item.price?.let { priceText(it) },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ExpiryText(item.expiryDate, today)
        }
        OverflowMenu(
            items = listOf(
                OverflowMenuItem(stringResource(R.string.stock_add_to_shopping), onAddToList),
                OverflowMenuItem(stringResource(R.string.action_delete), onDelete),
            ),
        )
    }
}
