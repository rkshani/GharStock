package com.rk.gharstock.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.database.entity.StoragePlaceEntity

data class ShoppingListWithCounts(
    @Embedded val list: ShoppingListEntity,
    val totalItems: Int,
    val boughtItems: Int,
)

data class ShoppingItemWithCategory(
    @Embedded val item: ShoppingItemEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: CategoryEntity?,
)

data class StockItemWithDetails(
    @Embedded val item: StockItemEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: CategoryEntity?,
    @Relation(parentColumn = "place_id", entityColumn = "id")
    val place: StoragePlaceEntity?,
)

data class CategorySpendingRow(
    val categoryName: String?,
    val total: Double,
)

data class MedicineWithSchedule(
    @Embedded val medicine: MedicineEntity,
    @Relation(parentColumn = "id", entityColumn = "medicine_id")
    val schedule: DoseScheduleEntity?,
)

data class MemberWithMedicinesRelation(
    @Embedded val member: FamilyMemberEntity,
    @Relation(
        entity = MedicineEntity::class,
        parentColumn = "id",
        entityColumn = "member_id",
    )
    val medicines: List<MedicineWithSchedule>,
)
