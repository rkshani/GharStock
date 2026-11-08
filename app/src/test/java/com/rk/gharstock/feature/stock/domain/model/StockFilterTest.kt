package com.rk.gharstock.feature.stock.domain.model

import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.core.model.StoragePlace
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StockFilterTest {

    private val dairy = Category(1, "Dairy")
    private val grains = Category(2, "Grains")
    private val fridge = StoragePlace(1, "Fridge")
    private val day = LocalDate.of(2026, 11, 8)

    private fun item(name: String, category: Category? = null, place: StoragePlace? = null, expiry: LocalDate? = null) =
        StockItem(
            name = name,
            quantity = 1.0,
            unit = MeasureUnit.PCS,
            category = category,
            place = place,
            purchaseDate = day,
            expiryDate = expiry,
        )

    private val items = listOf(
        item("milk", dairy, fridge, day.plusDays(2)),
        item("Basmati rice", grains, expiry = null),
        item("Yogurt", dairy, fridge, day.plusDays(1)),
        item("Atta", grains, expiry = day.plusDays(30)),
    )

    @Test
    fun `default sort is by name ignoring case`() {
        assertEquals(
            listOf("Atta", "Basmati rice", "milk", "Yogurt"),
            items.applyFilter(StockFilter()).map { it.name },
        )
    }

    @Test
    fun `expiry sort puts soonest first and no expiry last`() {
        assertEquals(
            listOf("Yogurt", "milk", "Atta", "Basmati rice"),
            items.applyFilter(StockFilter(sort = StockSort.EXPIRY)).map { it.name },
        )
    }

    @Test
    fun `search, category and place filters combine`() {
        assertEquals(listOf("Basmati rice"), items.applyFilter(StockFilter(query = " RICE ")).map { it.name })
        assertEquals(listOf("milk", "Yogurt"), items.applyFilter(StockFilter(categoryId = dairy.id)).map { it.name })
        assertEquals(
            listOf("Yogurt"),
            items.applyFilter(StockFilter(query = "yo", categoryId = dairy.id, placeId = fridge.id)).map { it.name },
        )
    }
}
