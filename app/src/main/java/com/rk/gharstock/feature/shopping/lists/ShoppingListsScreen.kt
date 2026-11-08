package com.rk.gharstock.feature.shopping.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.ConfirmDeleteDialog
import com.rk.gharstock.core.ui.component.EmptyState
import com.rk.gharstock.core.ui.component.LoadingState
import com.rk.gharstock.core.ui.component.NameInputDialog
import com.rk.gharstock.core.ui.component.OverflowMenu
import com.rk.gharstock.core.ui.component.OverflowMenuItem
import com.rk.gharstock.feature.shopping.domain.model.ShoppingListSummary

@Composable
fun ShoppingListsScreen(
    onOpenList: (Long) -> Unit,
    viewModel: ShoppingListsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ShoppingListsContent(state = state, onEvent = viewModel::onEvent, onOpenList = onOpenList)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListsContent(
    state: ShoppingListsUiState,
    onEvent: (ShoppingListsEvent) -> Unit,
    onOpenList: (Long) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.shopping_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(ShoppingListsEvent.AddListClicked) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.shopping_new_list)) },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingState(modifier)
            state.lists.isEmpty() -> EmptyState(
                icon = Icons.AutoMirrored.Outlined.ListAlt,
                title = stringResource(R.string.shopping_empty_title),
                message = stringResource(R.string.shopping_empty_message),
                modifier = modifier,
            )
            else -> LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.lists, key = { it.list.id }) { summary ->
                    ShoppingListCard(
                        modifier = Modifier.animateItem(),
                        summary = summary,
                        onClick = { onOpenList(summary.list.id) },
                        onRename = { onEvent(ShoppingListsEvent.RenameClicked(summary)) },
                        onDelete = { onEvent(ShoppingListsEvent.DeleteClicked(summary)) },
                    )
                }
            }
        }
    }

    state.nameDialog?.let { dialog ->
        NameInputDialog(
            title = stringResource(if (dialog.isNew) R.string.shopping_new_list else R.string.shopping_rename_list),
            label = stringResource(R.string.shopping_list_name_label),
            state = dialog,
            onTextChange = { onEvent(ShoppingListsEvent.DialogTextChanged(it)) },
            onConfirm = { onEvent(ShoppingListsEvent.DialogConfirmed) },
            onDismiss = { onEvent(ShoppingListsEvent.DialogDismissed) },
        )
    }
    state.deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            itemName = target.list.name,
            message = stringResource(R.string.shopping_delete_list_message),
            onConfirm = { onEvent(ShoppingListsEvent.DeleteConfirmed) },
            onDismiss = { onEvent(ShoppingListsEvent.DeleteDismissed) },
        )
    }
}

@Composable
private fun ShoppingListCard(
    modifier: Modifier,
    summary: ShoppingListSummary,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(summary.list.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (summary.totalItems == 0) {
                        stringResource(R.string.shopping_list_empty_items)
                    } else {
                        stringResource(R.string.shopping_list_progress, summary.boughtItems, summary.totalItems)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (summary.totalItems > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { summary.boughtItems.toFloat() / summary.totalItems },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            OverflowMenu(
                items = listOf(
                    OverflowMenuItem(stringResource(R.string.action_rename), onRename),
                    OverflowMenuItem(stringResource(R.string.action_delete), onDelete),
                ),
            )
        }
    }
}
