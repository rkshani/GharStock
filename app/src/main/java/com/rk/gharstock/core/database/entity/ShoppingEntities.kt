package com.rk.gharstock.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rk.gharstock.core.model.MeasureUnit

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("list_id"), Index("category_id")],
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "list_id") val listId: Long,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    val price: Double?,
    @ColumnInfo(name = "is_bought") val isBought: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
