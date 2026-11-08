package com.rk.gharstock.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rk.gharstock.core.database.dao.BackupDao
import com.rk.gharstock.core.database.dao.CategoryDao
import com.rk.gharstock.core.database.dao.DoseLogDao
import com.rk.gharstock.core.database.dao.FamilyMemberDao
import com.rk.gharstock.core.database.dao.MedicineDao
import com.rk.gharstock.core.database.dao.ShoppingItemDao
import com.rk.gharstock.core.database.dao.ShoppingListDao
import com.rk.gharstock.core.database.dao.StockItemDao
import com.rk.gharstock.core.database.dao.StoragePlaceDao
import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.database.entity.StoragePlaceEntity

@Database(
    entities = [
        CategoryEntity::class,
        StoragePlaceEntity::class,
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        StockItemEntity::class,
        FamilyMemberEntity::class,
        MedicineEntity::class,
        DoseScheduleEntity::class,
        DoseLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GharStockDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun storagePlaceDao(): StoragePlaceDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun stockItemDao(): StockItemDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun medicineDao(): MedicineDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun backupDao(): BackupDao

    companion object {
        const val NAME = "ghar_stock.db"
    }
}
