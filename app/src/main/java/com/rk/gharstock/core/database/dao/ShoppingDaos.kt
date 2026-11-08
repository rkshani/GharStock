package com.rk.gharstock.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.relation.ShoppingItemWithCategory
import com.rk.gharstock.core.database.relation.ShoppingListWithCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query(
        """
        SELECT l.*,
               COUNT(i.id) AS totalItems,
               COALESCE(SUM(i.is_bought), 0) AS boughtItems
        FROM shopping_lists l
        LEFT JOIN shopping_items i ON i.list_id = l.id
        GROUP BY l.id
        ORDER BY l.created_at
        """,
    )
    fun observeWithCounts(): Flow<List<ShoppingListWithCounts>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeById(id: Long): Flow<ShoppingListEntity?>

    @Insert
    suspend fun insert(list: ShoppingListEntity): Long

    @Query("UPDATE shopping_lists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ShoppingItemDao {

    /** Pending items first, then bought; newest first inside each group. */
    @Transaction
    @Query("SELECT * FROM shopping_items WHERE list_id = :listId ORDER BY is_bought, created_at DESC")
    fun observeByList(listId: Long): Flow<List<ShoppingItemWithCategory>>

    @Transaction
    @Query("SELECT * FROM shopping_items WHERE list_id = :listId AND is_bought = 1")
    suspend fun getBought(listId: Long): List<ShoppingItemWithCategory>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE is_bought = 0")
    fun observePendingCount(): Flow<Int>

    @Insert
    suspend fun insert(item: ShoppingItemEntity): Long

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET is_bought = :bought WHERE id = :id")
    suspend fun setBought(id: Long, bought: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM shopping_items WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<Long>)
}
