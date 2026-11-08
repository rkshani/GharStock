package com.rk.gharstock.feature.stock.domain

import com.rk.gharstock.feature.stock.domain.model.StockItem
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun observeItems(): Flow<List<StockItem>>
    fun observeCount(): Flow<Int>

    /** Items expiring on or before [date], including already expired ones. */
    fun observeExpiringBy(date: LocalDate): Flow<List<StockItem>>
    suspend fun getItem(id: Long): StockItem?
    suspend fun saveItem(item: StockItem): Long
    suspend fun addItems(items: List<StockItem>)
    suspend fun deleteItem(id: Long)
}
