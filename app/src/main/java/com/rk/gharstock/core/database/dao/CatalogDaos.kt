package com.rk.gharstock.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.StoragePlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name COLLATE NOCASE AND id != :excludeId)")
    suspend fun nameExists(name: String, excludeId: Long): Boolean

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface StoragePlaceDao {

    @Query("SELECT * FROM storage_places ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<StoragePlaceEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM storage_places WHERE name = :name COLLATE NOCASE AND id != :excludeId)")
    suspend fun nameExists(name: String, excludeId: Long): Boolean

    @Insert
    suspend fun insert(place: StoragePlaceEntity): Long

    @Update
    suspend fun update(place: StoragePlaceEntity)

    @Query("DELETE FROM storage_places WHERE id = :id")
    suspend fun delete(id: Long)
}
