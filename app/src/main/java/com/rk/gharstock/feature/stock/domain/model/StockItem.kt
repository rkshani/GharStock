package com.rk.gharstock.feature.stock.domain.model

import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.core.model.StoragePlace
import java.time.LocalDate

data class StockItem(
    val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
    val category: Category? = null,
    val place: StoragePlace? = null,
    /** Total price paid for this purchase (not per unit). */
    val price: Double? = null,
    val purchaseDate: LocalDate,
    val expiryDate: LocalDate? = null,
    val createdAt: Long = 0,
)
