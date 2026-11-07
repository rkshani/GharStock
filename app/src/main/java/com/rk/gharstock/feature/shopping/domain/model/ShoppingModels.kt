package com.rk.gharstock.feature.shopping.domain.model

import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.MeasureUnit

data class ShoppingList(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

data class ShoppingListSummary(
    val list: ShoppingList,
    val totalItems: Int,
    val boughtItems: Int,
)

data class ShoppingItem(
    val id: Long = 0,
    val listId: Long,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
    val category: Category? = null,
    val price: Double? = null,
    val isBought: Boolean = false,
    val createdAt: Long = 0,
)
