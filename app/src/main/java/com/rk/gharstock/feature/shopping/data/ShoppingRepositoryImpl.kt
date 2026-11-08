package com.rk.gharstock.feature.shopping.data

import com.rk.gharstock.core.database.dao.ShoppingItemDao
import com.rk.gharstock.core.database.dao.ShoppingListDao
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.mapper.toModel
import com.rk.gharstock.core.database.relation.ShoppingItemWithCategory
import com.rk.gharstock.core.database.relation.ShoppingListWithCounts
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.shopping.domain.model.ShoppingList
import com.rk.gharstock.feature.shopping.domain.model.ShoppingListSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingRepositoryImpl @Inject constructor(
    private val listDao: ShoppingListDao,
    private val itemDao: ShoppingItemDao,
) : ShoppingRepository {

    override fun observeLists(): Flow<List<ShoppingListSummary>> =
        listDao.observeWithCounts().map { lists -> lists.map { it.toModel() } }

    override fun observeList(id: Long): Flow<ShoppingList?> =
        listDao.observeById(id).map { it?.toModel() }

    override suspend fun createList(list: ShoppingList): Long = listDao.insert(list.toEntity())

    override suspend fun renameList(id: Long, name: String) = listDao.rename(id, name)

    override suspend fun deleteList(id: Long) = listDao.delete(id)

    override fun observeItems(listId: Long): Flow<List<ShoppingItem>> =
        itemDao.observeByList(listId).map { items -> items.map { it.toModel() } }

    override fun observePendingCount(): Flow<Int> = itemDao.observePendingCount()

    override suspend fun getBoughtItems(listId: Long): List<ShoppingItem> =
        itemDao.getBought(listId).map { it.toModel() }

    override suspend fun saveItem(item: ShoppingItem): Long =
        if (item.id == 0L) {
            itemDao.insert(item.toEntity())
        } else {
            itemDao.update(item.toEntity())
            item.id
        }

    override suspend fun setBought(id: Long, bought: Boolean) = itemDao.setBought(id, bought)

    override suspend fun deleteItem(id: Long) = itemDao.delete(id)

    override suspend fun deleteItems(ids: List<Long>) = itemDao.deleteAll(ids)
}

private fun ShoppingListEntity.toModel() = ShoppingList(id = id, name = name, createdAt = createdAt)

private fun ShoppingList.toEntity() = ShoppingListEntity(id = id, name = name, createdAt = createdAt)

private fun ShoppingListWithCounts.toModel() = ShoppingListSummary(
    list = list.toModel(),
    totalItems = totalItems,
    boughtItems = boughtItems,
)

private fun ShoppingItemWithCategory.toModel() = ShoppingItem(
    id = item.id,
    listId = item.listId,
    name = item.name,
    quantity = item.quantity,
    unit = item.unit,
    category = category?.toModel(),
    price = item.price,
    isBought = item.isBought,
    createdAt = item.createdAt,
)

private fun ShoppingItem.toEntity() = ShoppingItemEntity(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    categoryId = category?.id,
    price = price,
    isBought = isBought,
    createdAt = createdAt,
)
