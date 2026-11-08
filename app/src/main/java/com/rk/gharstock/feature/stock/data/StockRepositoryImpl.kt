package com.rk.gharstock.feature.stock.data

import com.rk.gharstock.core.database.dao.StockItemDao
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.database.mapper.toModel
import com.rk.gharstock.core.database.relation.StockItemWithDetails
import com.rk.gharstock.feature.stock.domain.StockRepository
import com.rk.gharstock.feature.stock.domain.model.StockItem
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StockRepositoryImpl @Inject constructor(
    private val dao: StockItemDao,
) : StockRepository {

    override fun observeItems(): Flow<List<StockItem>> =
        dao.observeAll().map { items -> items.map { it.toModel() } }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override fun observeExpiringBy(date: LocalDate): Flow<List<StockItem>> =
        dao.observeExpiringBy(date).map { items -> items.map { it.toModel() } }

    override suspend fun getItem(id: Long): StockItem? = dao.getById(id)?.toModel()

    override suspend fun saveItem(item: StockItem): Long =
        if (item.id == 0L) {
            dao.insert(item.toEntity())
        } else {
            dao.update(item.toEntity())
            item.id
        }

    override suspend fun addItems(items: List<StockItem>) = dao.insertAll(items.map { it.toEntity() })

    override suspend fun deleteItem(id: Long) = dao.delete(id)
}

private fun StockItemWithDetails.toModel() = StockItem(
    id = item.id,
    name = item.name,
    quantity = item.quantity,
    unit = item.unit,
    category = category?.toModel(),
    place = place?.toModel(),
    price = item.price,
    purchaseDate = item.purchaseDate,
    expiryDate = item.expiryDate,
    createdAt = item.createdAt,
)

private fun StockItem.toEntity() = StockItemEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    categoryId = category?.id,
    placeId = place?.id,
    price = price,
    purchaseDate = purchaseDate,
    expiryDate = expiryDate,
    createdAt = createdAt,
)
