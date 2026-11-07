package com.rk.gharstock.feature.shopping.domain

import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.shopping.domain.model.ShoppingList
import com.rk.gharstock.feature.shopping.domain.model.ShoppingListSummary
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun observeLists(): Flow<List<ShoppingListSummary>>
    fun observeList(id: Long): Flow<ShoppingList?>
    suspend fun createList(list: ShoppingList): Long
    suspend fun renameList(id: Long, name: String)
    suspend fun deleteList(id: Long)

    fun observeItems(listId: Long): Flow<List<ShoppingItem>>
    fun observePendingCount(): Flow<Int>
    suspend fun getBoughtItems(listId: Long): List<ShoppingItem>
    suspend fun saveItem(item: ShoppingItem): Long
    suspend fun setBought(id: Long, bought: Boolean)
    suspend fun deleteItem(id: Long)
    suspend fun deleteItems(ids: List<Long>)
}
