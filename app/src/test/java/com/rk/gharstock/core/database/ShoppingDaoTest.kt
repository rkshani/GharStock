package com.rk.gharstock.core.database

import app.cash.turbine.test
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingDaoTest : DatabaseTest() {

    private val listDao get() = db.shoppingListDao()
    private val itemDao get() = db.shoppingItemDao()

    @Test
    fun `lists include total and bought item counts`() = runTest {
        val kiryana = listDao.insert(ShoppingListEntity(name = "Kiryana", createdAt = 1))
        val sabzi = listDao.insert(ShoppingListEntity(name = "Sabzi", createdAt = 2))
        itemDao.insert(shoppingItem(kiryana, bought = true))
        itemDao.insert(shoppingItem(kiryana))
        itemDao.insert(shoppingItem(kiryana))

        val lists = listDao.observeWithCounts().first()

        assertEquals(listOf(kiryana, sabzi), lists.map { it.list.id })
        assertEquals(3, lists[0].totalItems)
        assertEquals(1, lists[0].boughtItems)
        assertEquals(0, lists[1].totalItems)
        assertEquals(0, lists[1].boughtItems)
    }

    @Test
    fun `items show pending first then newest first`() = runTest {
        val listId = listDao.insert(ShoppingListEntity(name = "Kiryana", createdAt = 0))
        itemDao.insert(shoppingItem(listId, name = "Old", createdAt = 1))
        itemDao.insert(shoppingItem(listId, name = "Bought", bought = true, createdAt = 3))
        itemDao.insert(shoppingItem(listId, name = "New", createdAt = 2))

        val names = itemDao.observeByList(listId).first().map { it.item.name }

        assertEquals(listOf("New", "Old", "Bought"), names)
    }

    @Test
    fun `deleting a list deletes its items`() = runTest {
        val listId = listDao.insert(ShoppingListEntity(name = "Kiryana", createdAt = 0))
        itemDao.insert(shoppingItem(listId))

        listDao.delete(listId)

        assertTrue(itemDao.observeByList(listId).first().isEmpty())
    }

    @Test
    fun `pending count updates when an item is ticked`() = runTest {
        val listId = listDao.insert(ShoppingListEntity(name = "Kiryana", createdAt = 0))
        val itemId = itemDao.insert(shoppingItem(listId))

        itemDao.observePendingCount().test {
            assertEquals(1, awaitItem())
            itemDao.setBought(itemId, true)
            assertEquals(0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
