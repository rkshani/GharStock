package com.rk.gharstock.feature.shopping.domain.usecase

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.ValidationException
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.feature.shopping.data.ShoppingRepositoryImpl
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.stock.data.StockRepositoryImpl
import com.rk.gharstock.feature.stock.domain.model.StockItem
import com.rk.gharstock.feature.stock.domain.usecase.AddStockItemToShoppingListUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingUseCasesTest : DatabaseTest() {

    private val shoppingRepo by lazy { ShoppingRepositoryImpl(db.shoppingListDao(), db.shoppingItemDao()) }
    private val stockRepo by lazy { StockRepositoryImpl(db.stockItemDao()) }

    private val saveList by lazy { SaveShoppingListUseCase(shoppingRepo, clock) }
    private val saveItem by lazy { SaveShoppingItemUseCase(shoppingRepo, clock) }
    private val moveToStock by lazy { MoveBoughtItemsToStockUseCase(shoppingRepo, stockRepo, transaction, clock) }

    private fun item(listId: Long, name: String, quantity: Double = 2.0, price: Double? = null) =
        ShoppingItem(listId = listId, name = name, quantity = quantity, unit = MeasureUnit.KG, price = price)

    private fun ValidationError.assertFailureOf(result: Result<*>) {
        assertEquals(this, (result.exceptionOrNull() as ValidationException).error)
    }

    @Test
    fun `list name is trimmed and blank names are rejected`() = runTest {
        val id = saveList(0, "  Kiryana ").getOrThrow()
        ValidationError.BLANK_NAME.assertFailureOf(saveList(0, "   "))

        saveList(id, "Medical Store").getOrThrow()

        assertEquals("Medical Store", shoppingRepo.observeList(id).first()?.name)
    }

    @Test
    fun `item validation rejects bad quantity and price`() = runTest {
        val listId = saveList(0, "Kiryana").getOrThrow()

        ValidationError.BLANK_NAME.assertFailureOf(saveItem(item(listId, " ")))
        ValidationError.INVALID_QUANTITY.assertFailureOf(saveItem(item(listId, "Rice", quantity = 0.0)))
        ValidationError.INVALID_PRICE.assertFailureOf(saveItem(item(listId, "Rice", price = -1.0)))
        assertTrue(saveItem(item(listId, "Rice", price = 450.0)).isSuccess)
    }

    @Test
    fun `move bought items adds them to stock and removes them from the list`() = runTest {
        val listId = saveList(0, "Kiryana").getOrThrow()
        val riceId = saveItem(item(listId, "Rice", price = 900.0)).getOrThrow()
        saveItem(item(listId, "Sugar")).getOrThrow()
        shoppingRepo.setBought(riceId, true)

        val moved = moveToStock(listId)

        assertEquals(1, moved)
        assertEquals(listOf("Sugar"), shoppingRepo.observeItems(listId).first().map { it.name })
        val stock = stockRepo.observeItems().first().single()
        assertEquals("Rice", stock.name)
        assertEquals(900.0, stock.price!!, 0.0)
        assertEquals(today, stock.purchaseDate)
    }

    @Test
    fun `move with nothing bought does nothing`() = runTest {
        val listId = saveList(0, "Kiryana").getOrThrow()
        saveItem(item(listId, "Sugar")).getOrThrow()

        assertEquals(0, moveToStock(listId))
        assertTrue(stockRepo.observeItems().first().isEmpty())
    }

    @Test
    fun `stock item can be added to a shopping list`() = runTest {
        val listId = saveList(0, "Kiryana").getOrThrow()
        val stockItem = StockItem(name = "Oil", quantity = 1.0, unit = MeasureUnit.L, purchaseDate = today)
        val addToList = AddStockItemToShoppingListUseCase(shoppingRepo, clock)

        addToList(stockItem, listId, quantity = 3.0).getOrThrow()

        val added = shoppingRepo.observeItems(listId).first().single()
        assertEquals("Oil", added.name)
        assertEquals(3.0, added.quantity, 0.0)
        assertEquals(MeasureUnit.L, added.unit)
        assertEquals(false, added.isBought)
    }
}
