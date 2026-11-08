package com.rk.gharstock.feature.shopping.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.ConfirmDeleteDialog
import com.rk.gharstock.core.ui.component.EmptyState
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.SectionHeader
import com.rk.gharstock.core.ui.component.UserMessageEffect
import com.rk.gharstock.core.ui.component.semanticsLabel
import com.rk.gharstock.core.ui.text.priceText
import com.rk.gharstock.core.ui.text.quantityText
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem

@Composable
fun ShoppingListDetailScreen(
    onBack: () -> Unit,
    viewModel: ShoppingListDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isListDeleted) { if (state.isListDeleted) onBack() }
    ShoppingListDetailContent(state = state, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListDetailContent(
    state: ShoppingListDetailUiState,
    onEvent: (ShoppingListDetailEvent) -> Unit,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(state.message, snackbarHostState) { onEvent(ShoppingListDetailEvent.MessageShown) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.listName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (state.bought.isNotEmpty()) {
                        IconButton(onClick = { onEvent(ShoppingListDetailEvent.MoveToStockClicked) }) {
                            Icon(
                                Icons.Outlined.MoveToInbox,
                                contentDescription = stringResource(R.string.shopping_move_to_stock),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(ShoppingListDetailEvent.AddItemClicked) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.shopping_add_item))
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingState(modifier)
            state.isEmpty -> EmptyState(
                icon = Icons.Outlined.ShoppingCart,
                title = stringResource(R.string.shopping_items_empty_title),
                message = stringResource(R.string.shopping_items_empty_message),
                modifier = modifier,
            )
            else -> LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 88.dp)) {
                itemSection(R.string.shopping_section_to_buy, state.toBuy, onEvent)
                itemSection(R.string.shopping_section_bought, state.bought, onEvent)
            }
        }
    }

    state.form?.let { form ->
        ShoppingItemSheet(
            form = form,
            categories = state.categories,
            onChange = { onEvent(ShoppingListDetailEvent.FormChanged(it)) },
            onSave = { onEvent(ShoppingListDetailEvent.SaveItem) },
            onDismiss = { onEvent(ShoppingListDetailEvent.DismissForm) },
        )
    }
    if (state.showMoveConfirm) {
        AlertDialog(
            onDismissRequest = { onEvent(ShoppingListDetailEvent.MoveToStockDismissed) },
            title = { Text(stringResource(R.string.shopping_move_to_stock)) },
            text = { Text(stringResource(R.string.shopping_move_to_stock_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ShoppingListDetailEvent.MoveToStockConfirmed) }) {
                    Text(stringResource(R.string.action_move))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ShoppingListDetailEvent.MoveToStockDismissed) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
    state.deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            itemName = target.name,
            onConfirm = { onEvent(ShoppingListDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(ShoppingListDetailEvent.DeleteDismissed) },
        )
    }
}

private fun LazyListScope.itemSection(
    titleRes: Int,
    items: List<ShoppingItem>,
    onEvent: (ShoppingListDetailEvent) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = "header_$titleRes") { SectionHeader(stringResource(titleRes)) }
    items(items, key = { it.id }) { item ->
        ShoppingItemRow(
            item = item,
            onToggle = { onEvent(ShoppingListDetailEvent.BoughtToggled(item)) },
            onEdit = { onEvent(ShoppingListDetailEvent.EditItemClicked(item)) },
            onDelete = { onEvent(ShoppingListDetailEvent.DeleteClicked(item)) },
        )
        HorizontalDivider()
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit, role = Role.Button)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val toggleLabel = stringResource(R.string.shopping_mark_bought, item.name)
        Checkbox(
            checked = item.isBought,
            onCheckedChange = { onToggle() },
            modifier = Modifier.semanticsLabel(toggleLabel),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isBought) TextDecoration.LineThrough else null,
                color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = listOfNotNull(
                    quantityText(item.quantity, item.unit),
                    item.category?.name,
                    item.price?.let { priceText(it) },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.shopping_delete_item, item.name))
        }
    }
}
