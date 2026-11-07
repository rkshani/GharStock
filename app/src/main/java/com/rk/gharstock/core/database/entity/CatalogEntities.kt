package com.rk.gharstock.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "storage_places",
    indices = [Index(value = ["name"], unique = true)],
)
data class StoragePlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)
