package com.rk.gharstock.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.database.entity.StoragePlaceEntity

/** Every table as it is stored, for backup and restore. */
data class DatabaseSnapshot(
    val categories: List<CategoryEntity>,
    val places: List<StoragePlaceEntity>,
    val shoppingLists: List<ShoppingListEntity>,
    val shoppingItems: List<ShoppingItemEntity>,
    val stockItems: List<StockItemEntity>,
    val members: List<FamilyMemberEntity>,
    val medicines: List<MedicineEntity>,
    val schedules: List<DoseScheduleEntity>,
    val doseLogs: List<DoseLogEntity>,
)

@Dao
abstract class BackupDao {

    @Transaction
    open suspend fun snapshot() = DatabaseSnapshot(
        categories = categories(),
        places = places(),
        shoppingLists = shoppingLists(),
        shoppingItems = shoppingItems(),
        stockItems = stockItems(),
        members = members(),
        medicines = medicines(),
        schedules = schedules(),
        doseLogs = doseLogs(),
    )

    /** Deletes everything and inserts [snapshot], keeping its ids. Children are deleted first. */
    @Transaction
    open suspend fun replaceAll(snapshot: DatabaseSnapshot) {
        deleteDoseLogs()
        deleteSchedules()
        deleteMedicines()
        deleteMembers()
        deleteShoppingItems()
        deleteShoppingLists()
        deleteStockItems()
        deleteCategories()
        deletePlaces()

        insertCategories(snapshot.categories)
        insertPlaces(snapshot.places)
        insertStockItems(snapshot.stockItems)
        insertShoppingLists(snapshot.shoppingLists)
        insertShoppingItems(snapshot.shoppingItems)
        insertMembers(snapshot.members)
        insertMedicines(snapshot.medicines)
        insertSchedules(snapshot.schedules)
        insertDoseLogs(snapshot.doseLogs)
    }

    @Query("SELECT * FROM categories") protected abstract suspend fun categories(): List<CategoryEntity>
    @Query("SELECT * FROM storage_places") protected abstract suspend fun places(): List<StoragePlaceEntity>
    @Query("SELECT * FROM shopping_lists") protected abstract suspend fun shoppingLists(): List<ShoppingListEntity>
    @Query("SELECT * FROM shopping_items") protected abstract suspend fun shoppingItems(): List<ShoppingItemEntity>
    @Query("SELECT * FROM stock_items") protected abstract suspend fun stockItems(): List<StockItemEntity>
    @Query("SELECT * FROM family_members") protected abstract suspend fun members(): List<FamilyMemberEntity>
    @Query("SELECT * FROM medicines") protected abstract suspend fun medicines(): List<MedicineEntity>
    @Query("SELECT * FROM dose_schedules") protected abstract suspend fun schedules(): List<DoseScheduleEntity>
    @Query("SELECT * FROM dose_logs") protected abstract suspend fun doseLogs(): List<DoseLogEntity>

    @Query("DELETE FROM dose_logs") protected abstract suspend fun deleteDoseLogs()
    @Query("DELETE FROM dose_schedules") protected abstract suspend fun deleteSchedules()
    @Query("DELETE FROM medicines") protected abstract suspend fun deleteMedicines()
    @Query("DELETE FROM family_members") protected abstract suspend fun deleteMembers()
    @Query("DELETE FROM shopping_items") protected abstract suspend fun deleteShoppingItems()
    @Query("DELETE FROM shopping_lists") protected abstract suspend fun deleteShoppingLists()
    @Query("DELETE FROM stock_items") protected abstract suspend fun deleteStockItems()
    @Query("DELETE FROM categories") protected abstract suspend fun deleteCategories()
    @Query("DELETE FROM storage_places") protected abstract suspend fun deletePlaces()

    @Insert protected abstract suspend fun insertCategories(items: List<CategoryEntity>)
    @Insert protected abstract suspend fun insertPlaces(items: List<StoragePlaceEntity>)
    @Insert protected abstract suspend fun insertShoppingLists(items: List<ShoppingListEntity>)
    @Insert protected abstract suspend fun insertShoppingItems(items: List<ShoppingItemEntity>)
    @Insert protected abstract suspend fun insertStockItems(items: List<StockItemEntity>)
    @Insert protected abstract suspend fun insertMembers(items: List<FamilyMemberEntity>)
    @Insert protected abstract suspend fun insertMedicines(items: List<MedicineEntity>)
    @Insert protected abstract suspend fun insertSchedules(items: List<DoseScheduleEntity>)
    @Insert protected abstract suspend fun insertDoseLogs(items: List<DoseLogEntity>)
}
