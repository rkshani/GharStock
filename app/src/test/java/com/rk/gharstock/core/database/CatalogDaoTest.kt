package com.rk.gharstock.core.database

import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogDaoTest : DatabaseTest() {

    @Test
    fun `default categories and places are seeded on create`() = runTest {
        val categories = db.categoryDao().observeAll().first().map { it.name }
        val places = db.storagePlaceDao().observeAll().first().map { it.name }

        assertEquals(
            listOf("Cleaning", "Dairy", "Drinks", "Fruits", "Grains", "Other", "Snacks", "Spices", "Vegetables"),
            categories,
        )
        assertEquals(listOf("Freezer", "Fridge", "Kitchen", "Store Room"), places)
    }

    @Test
    fun `name exists check ignores case and the item being edited`() = runTest {
        val dao = db.categoryDao()
        val id = dao.insert(CategoryEntity(name = "Bakery"))

        assertTrue(dao.nameExists("bakery", excludeId = 0))
        assertFalse(dao.nameExists("BAKERY", excludeId = id))
        assertFalse(dao.nameExists("Meat", excludeId = 0))
    }

    @Test
    fun `deleting a category keeps items and clears their category`() = runTest {
        val categoryId = db.categoryDao().insert(CategoryEntity(name = "Bakery"))
        val placeId = db.storagePlaceDao().observeAll().first().first().id
        val stockId = db.stockItemDao().insert(stockItem(categoryId = categoryId, placeId = placeId))
        val listId = db.shoppingListDao().insert(ShoppingListEntity(name = "Kiryana", createdAt = 0))
        db.shoppingItemDao().insert(shoppingItem(listId, categoryId = categoryId))

        db.categoryDao().delete(categoryId)
        db.storagePlaceDao().delete(placeId)

        val stock = db.stockItemDao().getById(stockId)!!
        assertNull(stock.item.categoryId)
        assertNull(stock.item.placeId)
        assertNull(stock.category)
        val shopping = db.shoppingItemDao().observeByList(listId).first().single()
        assertNull(shopping.item.categoryId)
    }
}
