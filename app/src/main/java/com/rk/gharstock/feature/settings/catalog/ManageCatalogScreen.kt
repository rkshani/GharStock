package com.rk.gharstock.feature.settings.catalog

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.ConfirmDeleteDialog
import com.rk.gharstock.core.ui.component.ManageListRow
import com.rk.gharstock.core.ui.component.ManageListScaffold
import com.rk.gharstock.core.ui.component.NameInputDialog
import com.rk.gharstock.feature.settings.CatalogKind

private class CatalogTexts(
    val title: Int,
    val add: Int,
    val rename: Int,
    val emptyTitle: Int,
    val emptyMessage: Int,
    val deleteMessage: Int,
)

private fun CatalogKind.texts() = when (this) {
    CatalogKind.CATEGORY -> CatalogTexts(
        R.string.manage_categories, R.string.category_add, R.string.category_rename,
        R.string.category_empty_title, R.string.category_empty_message, R.string.category_delete_message,
    )
    CatalogKind.PLACE -> CatalogTexts(
        R.string.manage_places, R.string.place_add, R.string.place_rename,
        R.string.place_empty_title, R.string.place_empty_message, R.string.place_delete_message,
    )
}

@Composable
fun ManageCatalogScreen(
    onBack: () -> Unit,
    viewModel: ManageCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent
    val texts = state.kind.texts()

    ManageListScaffold(
        title = stringResource(texts.title),
        addLabel = stringResource(texts.add),
        isLoading = state.isLoading,
        isEmpty = state.entries.isEmpty(),
        emptyIcon = if (state.kind == CatalogKind.CATEGORY) Icons.Outlined.Category else Icons.Outlined.Kitchen,
        emptyTitle = stringResource(texts.emptyTitle),
        emptyMessage = stringResource(texts.emptyMessage),
        onBack = onBack,
        onAdd = { onEvent(ManageCatalogEvent.AddClicked) },
    ) {
        items(state.entries, key = { it.id }) { entry ->
            ManageListRow(
                title = entry.name,
                subtitle = null,
                editLabel = stringResource(R.string.action_rename),
                onEdit = { onEvent(ManageCatalogEvent.RenameClicked(entry)) },
                onDelete = { onEvent(ManageCatalogEvent.DeleteClicked(entry)) },
            )
        }
    }

    state.nameDialog?.let { dialog ->
        NameInputDialog(
            title = stringResource(if (dialog.isNew) texts.add else texts.rename),
            state = dialog,
            onTextChange = { onEvent(ManageCatalogEvent.DialogTextChanged(it)) },
            onConfirm = { onEvent(ManageCatalogEvent.DialogConfirmed) },
            onDismiss = { onEvent(ManageCatalogEvent.DialogDismissed) },
        )
    }
    state.deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            itemName = target.name,
            message = stringResource(texts.deleteMessage),
            onConfirm = { onEvent(ManageCatalogEvent.DeleteConfirmed) },
            onDismiss = { onEvent(ManageCatalogEvent.DeleteDismissed) },
        )
    }
}
