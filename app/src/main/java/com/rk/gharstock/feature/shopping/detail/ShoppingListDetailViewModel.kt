package com.rk.gharstock.feature.shopping.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.core.ui.text.formatEditableNumber
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.core.util.toDecimalOrNull
import com.rk.gharstock.core.util.toOptionalDecimal
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import com.rk.gharstock.feature.shopping.ShoppingListDetailRoute
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.shopping.domain.usecase.SaveShoppingItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Editable fields of the add/edit item bottom sheet. Numbers are kept as typed text. */
data class ShoppingItemForm(
    val id: Long = 0,
    val name: String = "",
    val quantity: String = "1",
    val unit: MeasureUnit = MeasureUnit.PCS,
    val category: Category? = null,
    val price: String = "",
    val isBought: Boolean = false,
    val createdAt: Long = 0,
    @StringRes val error: Int? = null,
) {
    val isNew: Boolean get() = id == 0L
}

data class ShoppingListDetailUiState(
    val isLoading: Boolean = true,
    val listName: String = "",
    val isListDeleted: Boolean = false,
    val toBuy: List<ShoppingItem> = emptyList(),
    val bought: List<ShoppingItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val form: ShoppingItemForm? = null,
    val deleteTarget: ShoppingItem? = null,
) {
    val isEmpty: Boolean get() = toBuy.isEmpty() && bought.isEmpty()
}

sealed interface ShoppingListDetailEvent {
    data object AddItemClicked : ShoppingListDetailEvent
    data class EditItemClicked(val item: ShoppingItem) : ShoppingListDetailEvent
    data class FormChanged(val form: ShoppingItemForm) : ShoppingListDetailEvent
    data object SaveItem : ShoppingListDetailEvent
    data object DismissForm : ShoppingListDetailEvent
    data class BoughtToggled(val item: ShoppingItem) : ShoppingListDetailEvent
    data class DeleteClicked(val item: ShoppingItem) : ShoppingListDetailEvent
    data object DeleteConfirmed : ShoppingListDetailEvent
    data object DeleteDismissed : ShoppingListDetailEvent
}

private data class DetailDialogs(
    val form: ShoppingItemForm? = null,
    val deleteTarget: ShoppingItem? = null,
)

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ShoppingRepository,
    catalogRepository: CatalogRepository,
    private val saveItem: SaveShoppingItemUseCase,
) : ViewModel() {

    private val listId = savedStateHandle.toRoute<ShoppingListDetailRoute>().listId
    private val dialogs = MutableStateFlow(DetailDialogs())

    val uiState: StateFlow<ShoppingListDetailUiState> = combine(
        repository.observeList(listId),
        repository.observeItems(listId),
        catalogRepository.observeCategories(),
        dialogs,
    ) { list, items, categories, dialogs ->
        val (bought, toBuy) = items.partition { it.isBought }
        ShoppingListDetailUiState(
            isLoading = false,
            listName = list?.name.orEmpty(),
            isListDeleted = list == null,
            toBuy = toBuy,
            bought = bought,
            categories = categories,
            form = dialogs.form,
            deleteTarget = dialogs.deleteTarget,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListDetailUiState())

    fun onEvent(event: ShoppingListDetailEvent) {
        when (event) {
            ShoppingListDetailEvent.AddItemClicked -> dialogs.update { it.copy(form = ShoppingItemForm()) }
            is ShoppingListDetailEvent.EditItemClicked -> dialogs.update { it.copy(form = event.item.toForm()) }
            is ShoppingListDetailEvent.FormChanged -> dialogs.update { it.copy(form = event.form.copy(error = null)) }
            ShoppingListDetailEvent.SaveItem -> save()
            ShoppingListDetailEvent.DismissForm -> dialogs.update { it.copy(form = null) }
            is ShoppingListDetailEvent.BoughtToggled -> viewModelScope.launch {
                repository.setBought(event.item.id, !event.item.isBought)
            }
            is ShoppingListDetailEvent.DeleteClicked -> dialogs.update { it.copy(deleteTarget = event.item) }
            ShoppingListDetailEvent.DeleteConfirmed -> delete()
            ShoppingListDetailEvent.DeleteDismissed -> dialogs.update { it.copy(deleteTarget = null) }
        }
    }

    private fun save() {
        val form = dialogs.value.form ?: return
        viewModelScope.launch {
            saveItem(form.toItem())
                .onSuccess { dialogs.update { it.copy(form = null) } }
                .onFailure { error -> dialogs.update { it.copy(form = form.copy(error = error.validationMessageRes())) } }
        }
    }

    private fun delete() {
        val target = dialogs.value.deleteTarget ?: return
        dialogs.update { it.copy(deleteTarget = null) }
        viewModelScope.launch { repository.deleteItem(target.id) }
    }

    private fun ShoppingItemForm.toItem() = ShoppingItem(
        id = id,
        listId = listId,
        name = name,
        quantity = quantity.toDecimalOrNull() ?: Double.NaN,
        unit = unit,
        category = category,
        price = price.toOptionalDecimal(),
        isBought = isBought,
        createdAt = createdAt,
    )

    private fun ShoppingItem.toForm() = ShoppingItemForm(
        id = id,
        name = name,
        quantity = formatEditableNumber(quantity),
        unit = unit,
        category = category,
        price = price?.let(::formatEditableNumber).orEmpty(),
        isBought = isBought,
        createdAt = createdAt,
    )
}
