package com.rk.gharstock.feature.stock.edit

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.core.ui.text.formatEditableNumber
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.core.util.toDecimalOrNull
import com.rk.gharstock.core.util.toOptionalDecimal
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import com.rk.gharstock.feature.stock.StockEditRoute
import com.rk.gharstock.feature.stock.domain.StockRepository
import com.rk.gharstock.feature.stock.domain.model.StockItem
import com.rk.gharstock.feature.stock.domain.usecase.SaveStockItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StockDateField { PURCHASE, EXPIRY }

data class StockItemForm(
    val name: String = "",
    val quantity: String = "1",
    val unit: MeasureUnit = MeasureUnit.PCS,
    val category: Category? = null,
    val place: StoragePlace? = null,
    val price: String = "",
    val purchaseDate: LocalDate,
    val expiryDate: LocalDate? = null,
)

data class StockEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val form: StockItemForm = StockItemForm(purchaseDate = LocalDate.MIN),
    val categories: List<Category> = emptyList(),
    val places: List<StoragePlace> = emptyList(),
    val datePicker: StockDateField? = null,
    val showDeleteConfirm: Boolean = false,
    @StringRes val error: Int? = null,
    val isFinished: Boolean = false,
)

sealed interface StockEditEvent {
    data class FormChanged(val form: StockItemForm) : StockEditEvent
    data class PickDate(val field: StockDateField) : StockEditEvent
    data class DatePicked(val date: LocalDate) : StockEditEvent
    data object DatePickerDismissed : StockEditEvent
    data object Save : StockEditEvent
    data object DeleteClicked : StockEditEvent
    data object DeleteConfirmed : StockEditEvent
    data object DeleteDismissed : StockEditEvent
}

private data class EditLocalState(
    val isLoading: Boolean = true,
    val form: StockItemForm,
    val createdAt: Long = 0,
    val datePicker: StockDateField? = null,
    val showDeleteConfirm: Boolean = false,
    @StringRes val error: Int? = null,
    val isFinished: Boolean = false,
)

@HiltViewModel
class StockEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StockRepository,
    catalogRepository: CatalogRepository,
    private val saveItem: SaveStockItemUseCase,
    clock: Clock,
) : ViewModel() {

    private val itemId = savedStateHandle.toRoute<StockEditRoute>().itemId
    private val local = MutableStateFlow(
        EditLocalState(isLoading = itemId != 0L, form = StockItemForm(purchaseDate = LocalDate.now(clock))),
    )

    val uiState: StateFlow<StockEditUiState> = combine(
        local,
        catalogRepository.observeCategories(),
        catalogRepository.observePlaces(),
    ) { local, categories, places ->
        StockEditUiState(
            isLoading = local.isLoading,
            isNew = itemId == 0L,
            form = local.form,
            categories = categories,
            places = places,
            datePicker = local.datePicker,
            showDeleteConfirm = local.showDeleteConfirm,
            error = local.error,
            isFinished = local.isFinished,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StockEditUiState())

    init {
        if (itemId != 0L) loadItem()
    }

    fun onEvent(event: StockEditEvent) {
        when (event) {
            is StockEditEvent.FormChanged -> local.update { it.copy(form = event.form, error = null) }
            is StockEditEvent.PickDate -> local.update { it.copy(datePicker = event.field) }
            is StockEditEvent.DatePicked -> local.update { state ->
                val form = when (state.datePicker) {
                    StockDateField.PURCHASE -> state.form.copy(purchaseDate = event.date)
                    StockDateField.EXPIRY -> state.form.copy(expiryDate = event.date)
                    null -> state.form
                }
                state.copy(form = form, datePicker = null)
            }
            StockEditEvent.DatePickerDismissed -> local.update { it.copy(datePicker = null) }
            StockEditEvent.Save -> save()
            StockEditEvent.DeleteClicked -> local.update { it.copy(showDeleteConfirm = true) }
            StockEditEvent.DeleteConfirmed -> viewModelScope.launch {
                repository.deleteItem(itemId)
                local.update { it.copy(showDeleteConfirm = false, isFinished = true) }
            }
            StockEditEvent.DeleteDismissed -> local.update { it.copy(showDeleteConfirm = false) }
        }
    }

    private fun loadItem() {
        viewModelScope.launch {
            val item = repository.getItem(itemId)
            local.update { state ->
                if (item == null) {
                    state.copy(isFinished = true)
                } else {
                    state.copy(isLoading = false, form = item.toForm(), createdAt = item.createdAt)
                }
            }
        }
    }

    private fun save() {
        val state = local.value
        val form = state.form
        val item = StockItem(
            id = itemId,
            name = form.name,
            quantity = form.quantity.toDecimalOrNull() ?: Double.NaN,
            unit = form.unit,
            category = form.category,
            place = form.place,
            price = form.price.toOptionalDecimal(),
            purchaseDate = form.purchaseDate,
            expiryDate = form.expiryDate,
            createdAt = state.createdAt,
        )
        viewModelScope.launch {
            saveItem(item)
                .onSuccess { local.update { it.copy(isFinished = true) } }
                .onFailure { error -> local.update { it.copy(error = error.validationMessageRes()) } }
        }
    }

    private fun StockItem.toForm() = StockItemForm(
        name = name,
        quantity = formatEditableNumber(quantity),
        unit = unit,
        category = category,
        place = place,
        price = price?.let(::formatEditableNumber).orEmpty(),
        purchaseDate = purchaseDate,
        expiryDate = expiryDate,
    )
}
