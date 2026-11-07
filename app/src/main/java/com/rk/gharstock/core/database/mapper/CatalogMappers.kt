package com.rk.gharstock.core.database.mapper

import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.StoragePlaceEntity
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace

fun CategoryEntity.toModel() = Category(id = id, name = name)

fun Category.toEntity() = CategoryEntity(id = id, name = name)

fun StoragePlaceEntity.toModel() = StoragePlace(id = id, name = name)

fun StoragePlace.toEntity() = StoragePlaceEntity(id = id, name = name)
