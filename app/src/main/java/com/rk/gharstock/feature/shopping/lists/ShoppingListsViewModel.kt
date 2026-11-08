package com.rk.gharstock.feature.shopping.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.core.ui.component.NameDialogState
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingListSummary
import com.rk.gharstock.feature.shopping.domain.usecase.SaveShoppingListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShoppingListsUiState(
    val isLoading: Boolean = true,
    val lists: List<ShoppingListSummary> = emptyList(),
    val nameDialog: NameDialogState? = null,
    val deleteTarget: ShoppingListSummary? = null,
)

sealed interface ShoppingListsEvent {
    data object AddListClicked : ShoppingListsEvent
    data class RenameClicked(val summary: ShoppingListSummary) : ShoppingListsEvent
    data class DialogTextChanged(val text: String) : ShoppingListsEvent
    data object DialogConfirmed : ShoppingListsEvent
    data object DialogDismissed : ShoppingListsEvent
    data class DeleteClicked(val summary: ShoppingListSummary) : ShoppingListsEvent
    data object DeleteConfirmed : ShoppingListsEvent
    data object DeleteDismissed : ShoppingListsEvent
}

private data class ListsDialogs(
    val nameDialog: NameDialogState? = null,
    val deleteTarget: ShoppingListSummary? = null,
)

@HiltViewModel
class ShoppingListsViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    private val saveList: SaveShoppingListUseCase,
) : ViewModel() {

    private val dialogs = MutableStateFlow(ListsDialogs())

    val uiState: StateFlow<ShoppingListsUiState> =
        combine(repository.observeLists(), dialogs) { lists, dialogs ->
            ShoppingListsUiState(
                isLoading = false,
                lists = lists,
                nameDialog = dialogs.nameDialog,
                deleteTarget = dialogs.deleteTarget,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListsUiState())

    fun onEvent(event: ShoppingListsEvent) {
        when (event) {
            ShoppingListsEvent.AddListClicked -> dialogs.update { it.copy(nameDialog = NameDialogState()) }
            is ShoppingListsEvent.RenameClicked -> dialogs.update {
                it.copy(nameDialog = NameDialogState(id = event.summary.list.id, text = event.summary.list.name))
            }
            is ShoppingListsEvent.DialogTextChanged -> dialogs.update {
                it.copy(nameDialog = it.nameDialog?.copy(text = event.text, error = null))
            }
            ShoppingListsEvent.DialogConfirmed -> saveDialog()
            ShoppingListsEvent.DialogDismissed -> dialogs.update { it.copy(nameDialog = null) }
            is ShoppingListsEvent.DeleteClicked -> dialogs.update { it.copy(deleteTarget = event.summary) }
            ShoppingListsEvent.DeleteConfirmed -> deleteTarget()
            ShoppingListsEvent.DeleteDismissed -> dialogs.update { it.copy(deleteTarget = null) }
        }
    }

    private fun saveDialog() {
        val dialog = dialogs.value.nameDialog ?: return
        viewModelScope.launch {
            saveList(dialog.id, dialog.text)
                .onSuccess { dialogs.update { it.copy(nameDialog = null) } }
                .onFailure { error ->
                    dialogs.update { it.copy(nameDialog = dialog.copy(error = error.validationMessageRes())) }
                }
        }
    }

    private fun deleteTarget() {
        val target = dialogs.value.deleteTarget ?: return
        dialogs.update { it.copy(deleteTarget = null) }
        viewModelScope.launch { repository.deleteList(target.list.id) }
    }
}
