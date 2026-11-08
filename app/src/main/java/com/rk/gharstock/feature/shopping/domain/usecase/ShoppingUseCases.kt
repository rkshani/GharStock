package com.rk.gharstock.feature.shopping.domain.usecase

import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.validate
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.shopping.domain.model.ShoppingItem
import com.rk.gharstock.feature.shopping.domain.model.ShoppingList
import java.time.Clock
import javax.inject.Inject

/** Creates a list when [id] is 0, otherwise renames it. */
class SaveShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long, name: String): Result<Long> = runCatching {
        val trimmed = name.trim()
        validate(trimmed.isNotEmpty(), ValidationError.BLANK_NAME)
        if (id == 0L) {
            repository.createList(ShoppingList(name = trimmed, createdAt = clock.millis()))
        } else {
            repository.renameList(id, trimmed)
            id
        }
    }
}

class SaveShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(item: ShoppingItem): Result<Long> = runCatching {
        val name = item.name.trim()
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        validate(item.quantity > 0, ValidationError.INVALID_QUANTITY)
        validate(item.price == null || item.price >= 0, ValidationError.INVALID_PRICE)
        val createdAt = if (item.id == 0L) clock.millis() else item.createdAt
        repository.saveItem(item.copy(name = name, createdAt = createdAt))
    }
}
