package com.rk.gharstock.feature.stock.list

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.R
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.core.ui.component.UserMessage
import com.rk.gharstock.core.ui.text.formatEditableNumber
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.core.util.toDecimalOrNull
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingList
import com.rk.gharstock.feature.stock.domain.StockRepository
import com.rk.gharstock.feature.stock.domain.model.StockFilter
import com.rk.gharstock.feature.stock.domain.model.StockItem
import com.rk.gharstock.feature.stock.domain.model.StockSort
import com.rk.gharstock.feature.stock.domain.model.applyFilter
import com.rk.gharstock.feature.stock.domain.usecase.AddStockItemToShoppingListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddToListDialogState(
    val item: StockItem,
    val selectedListId: Long? = null,
    val quantity: String = "1",
    @StringRes val error: Int? = null,
)

data class StockUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.MIN,
    val hasAnyItems: Boolean = false,
    val items: List<StockItem> = emptyList(),
    val filter: StockFilter = StockFilter(),
    val categories: List<Category> = emptyList(),
    val places: List<StoragePlace> = emptyList(),
    val shoppingLists: List<ShoppingList> = emptyList(),
    val deleteTarget: StockItem? = null,
    val addToList: AddToListDialogState? = null,
    val message: UserMessage? = null,
)

sealed interface StockEvent {
    data class QueryChanged(val query: String) : StockEvent
    data class CategoryFilterChanged(val categoryId: Long?) : StockEvent
    data class PlaceFilterChanged(val placeId: Long?) : StockEvent
    data object SortToggled : StockEvent
    data object ClearFilters : StockEvent
    data class DeleteClicked(val item: StockItem) : StockEvent
    data object DeleteConfirmed : StockEvent
    data object DeleteDismissed : StockEvent
    data class AddToListClicked(val item: StockItem) : StockEvent
    data class AddToListChanged(val dialog: AddToListDialogState) : StockEvent
    data object AddToListConfirmed : StockEvent
    data object AddToListDismissed : StockEvent
    data object MessageShown : StockEvent
}

private data class StockLocalState(
    val filter: StockFilter = StockFilter(),
    val deleteTarget: StockItem? = null,
    val addToList: AddToListDialogState? = null,
    val message: UserMessage? = null,
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    catalogRepository: CatalogRepository,
    shoppingRepository: ShoppingRepository,
    private val addToShoppingList: AddStockItemToShoppingListUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val local = MutableStateFlow(StockLocalState())

    private val catalog = combine(
        catalogRepository.observeCategories(),
        catalogRepository.observePlaces(),
        shoppingRepository.observeLists().map { lists -> lists.map { it.list } },
    ) { categories, places, lists -> Triple(categories, places, lists) }

    val uiState: StateFlow<StockUiState> =
        combine(stockRepository.observeItems(), catalog, local) { items, (categories, places, lists), local ->
            StockUiState(
                isLoading = false,
                today = LocalDate.now(clock),
                hasAnyItems = items.isNotEmpty(),
                items = items.applyFilter(local.filter),
                filter = local.filter,
                categories = categories,
                places = places,
                shoppingLists = lists,
                deleteTarget = local.deleteTarget,
                addToList = local.addToList,
                message = local.message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StockUiState())

    fun onEvent(event: StockEvent) {
        when (event) {
            is StockEvent.QueryChanged -> updateFilter { it.copy(query = event.query) }
            is StockEvent.CategoryFilterChanged -> updateFilter { it.copy(categoryId = event.categoryId) }
            is StockEvent.PlaceFilterChanged -> updateFilter { it.copy(placeId = event.placeId) }
            StockEvent.SortToggled -> updateFilter {
                it.copy(sort = if (it.sort == StockSort.NAME) StockSort.EXPIRY else StockSort.NAME)
            }
            StockEvent.ClearFilters -> updateFilter { StockFilter(sort = it.sort) }
            is StockEvent.DeleteClicked -> local.update { it.copy(deleteTarget = event.item) }
            StockEvent.DeleteConfirmed -> delete()
            StockEvent.DeleteDismissed -> local.update { it.copy(deleteTarget = null) }
            is StockEvent.AddToListClicked -> local.update {
                it.copy(
                    addToList = AddToListDialogState(
                        item = event.item,
                        selectedListId = uiState.value.shoppingLists.singleOrNull()?.id,
                        quantity = formatEditableNumber(event.item.quantity),
                    ),
                )
            }
            is StockEvent.AddToListChanged -> local.update { it.copy(addToList = event.dialog.copy(error = null)) }
            StockEvent.AddToListConfirmed -> addToList()
            StockEvent.AddToListDismissed -> local.update { it.copy(addToList = null) }
            StockEvent.MessageShown -> local.update { it.copy(message = null) }
        }
    }

    private fun updateFilter(transform: (StockFilter) -> StockFilter) {
        local.update { it.copy(filter = transform(it.filter)) }
    }

    private fun delete() {
        val target = local.value.deleteTarget ?: return
        local.update { it.copy(deleteTarget = null) }
        viewModelScope.launch { stockRepository.deleteItem(target.id) }
    }

    private fun addToList() {
        val dialog = local.value.addToList ?: return
        val list = uiState.value.shoppingLists.firstOrNull { it.id == dialog.selectedListId } ?: return
        viewModelScope.launch {
            addToShoppingList(dialog.item, list.id, dialog.quantity.toDecimalOrNull() ?: Double.NaN)
                .onSuccess {
                    local.update {
                        it.copy(addToList = null, message = UserMessage.Text(R.string.stock_added_to_list, list.name))
                    }
                }
                .onFailure { error ->
                    local.update { it.copy(addToList = dialog.copy(error = error.validationMessageRes())) }
                }
        }
    }
}
