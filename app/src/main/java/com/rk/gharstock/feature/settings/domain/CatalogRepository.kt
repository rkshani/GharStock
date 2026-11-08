package com.rk.gharstock.feature.settings.domain

import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace
import kotlinx.coroutines.flow.Flow

/** Categories and storage places that the user can manage. */
interface CatalogRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun categoryNameExists(name: String, excludeId: Long): Boolean
    suspend fun saveCategory(category: Category): Long
    suspend fun deleteCategory(id: Long)

    fun observePlaces(): Flow<List<StoragePlace>>
    suspend fun placeNameExists(name: String, excludeId: Long): Boolean
    suspend fun savePlace(place: StoragePlace): Long
    suspend fun deletePlace(id: Long)
}
