package com.rk.gharstock.feature.settings.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.core.ui.component.NameDialogState
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.feature.settings.CatalogKind
import com.rk.gharstock.feature.settings.ManageCatalogRoute
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import com.rk.gharstock.feature.settings.domain.usecase.SaveCategoryUseCase
import com.rk.gharstock.feature.settings.domain.usecase.SaveStoragePlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A category or storage place shown in the manage screen. */
data class CatalogEntry(val id: Long, val name: String)

data class ManageCatalogUiState(
    val kind: CatalogKind,
    val isLoading: Boolean = true,
    val entries: List<CatalogEntry> = emptyList(),
    val nameDialog: NameDialogState? = null,
    val deleteTarget: CatalogEntry? = null,
)

sealed interface ManageCatalogEvent {
    data object AddClicked : ManageCatalogEvent
    data class RenameClicked(val entry: CatalogEntry) : ManageCatalogEvent
    data class DialogTextChanged(val text: String) : ManageCatalogEvent
    data object DialogConfirmed : ManageCatalogEvent
    data object DialogDismissed : ManageCatalogEvent
    data class DeleteClicked(val entry: CatalogEntry) : ManageCatalogEvent
    data object DeleteConfirmed : ManageCatalogEvent
    data object DeleteDismissed : ManageCatalogEvent
}

private data class CatalogDialogs(
    val nameDialog: NameDialogState? = null,
    val deleteTarget: CatalogEntry? = null,
)

@HiltViewModel
class ManageCatalogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CatalogRepository,
    private val saveCategory: SaveCategoryUseCase,
    private val savePlace: SaveStoragePlaceUseCase,
) : ViewModel() {

    private val kind = savedStateHandle.toRoute<ManageCatalogRoute>().kind
    private val dialogs = MutableStateFlow(CatalogDialogs())

    private val entries = when (kind) {
        CatalogKind.CATEGORY -> repository.observeCategories().map { list -> list.map { CatalogEntry(it.id, it.name) } }
        CatalogKind.PLACE -> repository.observePlaces().map { list -> list.map { CatalogEntry(it.id, it.name) } }
    }

    val uiState: StateFlow<ManageCatalogUiState> = combine(entries, dialogs) { entries, dialogs ->
        ManageCatalogUiState(
            kind = kind,
            isLoading = false,
            entries = entries,
            nameDialog = dialogs.nameDialog,
            deleteTarget = dialogs.deleteTarget,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageCatalogUiState(kind))

    fun onEvent(event: ManageCatalogEvent) {
        when (event) {
            ManageCatalogEvent.AddClicked -> dialogs.update { it.copy(nameDialog = NameDialogState()) }
            is ManageCatalogEvent.RenameClicked -> dialogs.update {
                it.copy(nameDialog = NameDialogState(id = event.entry.id, text = event.entry.name))
            }
            is ManageCatalogEvent.DialogTextChanged -> dialogs.update {
                it.copy(nameDialog = it.nameDialog?.copy(text = event.text, error = null))
            }
            ManageCatalogEvent.DialogConfirmed -> save()
            ManageCatalogEvent.DialogDismissed -> dialogs.update { it.copy(nameDialog = null) }
            is ManageCatalogEvent.DeleteClicked -> dialogs.update { it.copy(deleteTarget = event.entry) }
            ManageCatalogEvent.DeleteConfirmed -> delete()
            ManageCatalogEvent.DeleteDismissed -> dialogs.update { it.copy(deleteTarget = null) }
        }
    }

    private fun save() {
        val dialog = dialogs.value.nameDialog ?: return
        viewModelScope.launch {
            val result = when (kind) {
                CatalogKind.CATEGORY -> saveCategory(Category(dialog.id, dialog.text))
                CatalogKind.PLACE -> savePlace(StoragePlace(dialog.id, dialog.text))
            }
            result
                .onSuccess { dialogs.update { it.copy(nameDialog = null) } }
                .onFailure { error ->
                    dialogs.update { it.copy(nameDialog = dialog.copy(error = error.validationMessageRes())) }
                }
        }
    }

    private fun delete() {
        val target = dialogs.value.deleteTarget ?: return
        dialogs.update { it.copy(deleteTarget = null) }
        viewModelScope.launch {
            when (kind) {
                CatalogKind.CATEGORY -> repository.deleteCategory(target.id)
                CatalogKind.PLACE -> repository.deletePlace(target.id)
            }
        }
    }
}
