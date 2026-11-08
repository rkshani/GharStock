package com.rk.gharstock.feature.settings.members

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
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

@Composable
fun FamilyMembersScreen(
    onBack: () -> Unit,
    viewModel: FamilyMembersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent

    ManageListScaffold(
        title = stringResource(R.string.manage_members),
        addLabel = stringResource(R.string.member_add),
        isLoading = state.isLoading,
        isEmpty = state.members.isEmpty(),
        emptyIcon = Icons.Outlined.People,
        emptyTitle = stringResource(R.string.member_empty_title),
        emptyMessage = stringResource(R.string.member_empty_message),
        onBack = onBack,
        onAdd = { onEvent(FamilyMembersEvent.AddClicked) },
    ) {
        items(state.members, key = { it.id }) { member ->
            ManageListRow(
                title = member.name,
                subtitle = member.relation,
                editLabel = stringResource(R.string.action_edit),
                onEdit = { onEvent(FamilyMembersEvent.EditClicked(member)) },
                onDelete = { onEvent(FamilyMembersEvent.DeleteClicked(member)) },
            )
        }
    }

    state.dialog?.let { dialog ->
        NameInputDialog(
            title = stringResource(if (dialog.isNew) R.string.member_add else R.string.member_edit),
            state = dialog,
            onTextChange = { onEvent(FamilyMembersEvent.NameChanged(it)) },
            onConfirm = { onEvent(FamilyMembersEvent.DialogConfirmed) },
            onDismiss = { onEvent(FamilyMembersEvent.DialogDismissed) },
            secondaryLabel = stringResource(R.string.member_relation_label),
            onSecondaryTextChange = { onEvent(FamilyMembersEvent.RelationChanged(it)) },
        )
    }
    state.deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            itemName = target.name,
            message = stringResource(R.string.member_delete_message),
            onConfirm = { onEvent(FamilyMembersEvent.DeleteConfirmed) },
            onDismiss = { onEvent(FamilyMembersEvent.DeleteDismissed) },
        )
    }
}
