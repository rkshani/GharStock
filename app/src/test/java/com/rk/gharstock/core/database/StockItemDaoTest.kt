package com.rk.gharstock.core.database

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StockItemDaoTest : DatabaseTest() {

    private val dao get() = db.stockItemDao()

    @Test
    fun `item loads with its category and place`() = runTest {
        val category = db.categoryDao().observeAll().first().first { it.name == "Dairy" }
        val place = db.storagePlaceDao().observeAll().first().first { it.name == "Fridge" }
        val id = dao.insert(stockItem(categoryId = category.id, placeId = place.id))

        val loaded = dao.getById(id)!!

        assertEquals("Dairy", loaded.category?.name)
        assertEquals("Fridge", loaded.place?.name)
    }

    @Test
    fun `expiring query returns expired and soon items sorted by date`() = runTest {
        dao.insert(stockItem(name = "Soon", expiryDate = today.plusDays(2)))
        dao.insert(stockItem(name = "Expired", expiryDate = today.minusDays(1)))
        dao.insert(stockItem(name = "Later", expiryDate = today.plusDays(10)))
        dao.insert(stockItem(name = "NoExpiry"))

        val names = dao.observeExpiringBy(today.plusDays(3)).first().map { it.item.name }

        assertEquals(listOf("Expired", "Soon"), names)
    }

    @Test
    fun `count reflects inserted items`() = runTest {
        dao.insertAll(listOf(stockItem(), stockItem(name = "Rice")))

        assertEquals(2, dao.observeCount().first())
    }
}
