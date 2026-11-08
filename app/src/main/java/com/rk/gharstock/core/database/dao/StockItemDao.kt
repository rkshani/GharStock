package com.rk.gharstock.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.database.relation.StockItemWithDetails
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface StockItemDao {

    @Transaction
    @Query("SELECT * FROM stock_items ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<StockItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM stock_items WHERE id = :id")
    suspend fun getById(id: Long): StockItemWithDetails?

    /** Items with an expiry date on or before [date], soonest first (includes expired items). */
    @Transaction
    @Query("SELECT * FROM stock_items WHERE expiry_date IS NOT NULL AND expiry_date <= :date ORDER BY expiry_date")
    fun observeExpiringBy(date: LocalDate): Flow<List<StockItemWithDetails>>

    @Query("SELECT COUNT(*) FROM stock_items")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(item: StockItemEntity): Long

    @Insert
    suspend fun insertAll(items: List<StockItemEntity>)

    @Update
    suspend fun update(item: StockItemEntity)

    @Query("DELETE FROM stock_items WHERE id = :id")
    suspend fun delete(id: Long)
}
