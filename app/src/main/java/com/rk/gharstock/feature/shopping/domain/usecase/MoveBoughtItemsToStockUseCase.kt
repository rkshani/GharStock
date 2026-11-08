package com.rk.gharstock.feature.shopping.domain.usecase

import com.rk.gharstock.core.domain.TransactionRunner
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.stock.domain.StockRepository
import com.rk.gharstock.feature.stock.domain.model.StockItem
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Adds every bought item of a shopping list to stock (purchased today) and removes them
 * from the list. Returns how many items were moved.
 */
class MoveBoughtItemsToStockUseCase @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val stockRepository: StockRepository,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) {
    suspend operator fun invoke(listId: Long): Int = transaction {
        val bought = shoppingRepository.getBoughtItems(listId)
        if (bought.isNotEmpty()) {
            val today = LocalDate.now(clock)
            val now = clock.millis()
            stockRepository.addItems(bought.map { it.toStockItem(today, now) })
            shoppingRepository.deleteItems(bought.map { it.id })
        }
        bought.size
    }

    private fun ShoppingItem.toStockItem(today: LocalDate, now: Long) = StockItem(
        name = name,
        quantity = quantity,
        unit = unit,
        category = category,
        price = price,
        purchaseDate = today,
        createdAt = now,
    )
}
