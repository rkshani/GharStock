package com.rk.gharstock.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rk.gharstock.core.model.MeasureUnit
import java.time.LocalDate

@Entity(
    tableName = "stock_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = StoragePlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["place_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("category_id"), Index("place_id"), Index("expiry_date")],
)
data class StockItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "place_id") val placeId: Long?,
    /** Total price paid for this purchase (not per unit). */
    val price: Double?,
    @ColumnInfo(name = "purchase_date") val purchaseDate: LocalDate,
    @ColumnInfo(name = "expiry_date") val expiryDate: LocalDate?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
