package com.rk.gharstock.feature.settings.data

import com.rk.gharstock.core.database.dao.CategoryDao
import com.rk.gharstock.core.database.dao.StoragePlaceDao
import com.rk.gharstock.core.database.mapper.toEntity
import com.rk.gharstock.core.database.mapper.toModel
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CatalogRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val placeDao: StoragePlaceDao,
) : CatalogRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun categoryNameExists(name: String, excludeId: Long): Boolean =
        categoryDao.nameExists(name, excludeId)

    override suspend fun saveCategory(category: Category): Long =
        if (category.id == 0L) {
            categoryDao.insert(category.toEntity())
        } else {
            categoryDao.update(category.toEntity())
            category.id
        }

    override suspend fun deleteCategory(id: Long) = categoryDao.delete(id)

    override fun observePlaces(): Flow<List<StoragePlace>> =
        placeDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun placeNameExists(name: String, excludeId: Long): Boolean =
        placeDao.nameExists(name, excludeId)

    override suspend fun savePlace(place: StoragePlace): Long =
        if (place.id == 0L) {
            placeDao.insert(place.toEntity())
        } else {
            placeDao.update(place.toEntity())
            place.id
        }

    override suspend fun deletePlace(id: Long) = placeDao.delete(id)
}
