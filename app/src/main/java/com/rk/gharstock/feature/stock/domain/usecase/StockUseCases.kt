package com.rk.gharstock.feature.stock.domain.usecase

import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.validate
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.stock.domain.StockRepository
import com.rk.gharstock.feature.stock.domain.model.StockItem
import java.time.Clock
import javax.inject.Inject

class SaveStockItemUseCase @Inject constructor(
    private val repository: StockRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(item: StockItem): Result<Long> = runCatching {
        val name = item.name.trim()
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        validate(item.quantity > 0, ValidationError.INVALID_QUANTITY)
        validate(item.price == null || item.price >= 0, ValidationError.INVALID_PRICE)
        val createdAt = if (item.id == 0L) clock.millis() else item.createdAt
        repository.saveItem(item.copy(name = name, createdAt = createdAt))
    }
}

/** Manual low-stock flow: copies a stock item into a shopping list as a pending item. */
class AddStockItemToShoppingListUseCase @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(item: StockItem, listId: Long, quantity: Double): Result<Long> = runCatching {
        validate(quantity > 0, ValidationError.INVALID_QUANTITY)
        shoppingRepository.saveItem(
            ShoppingItem(
                listId = listId,
                name = item.name,
                quantity = quantity,
                unit = item.unit,
                category = item.category,
                createdAt = clock.millis(),
            ),
        )
    }
}
