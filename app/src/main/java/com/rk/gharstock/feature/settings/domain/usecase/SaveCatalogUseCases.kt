package com.rk.gharstock.feature.settings.domain.usecase

import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.validate
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import javax.inject.Inject

/** Adds or renames a category. Names must be non-blank and unique (ignoring case). */
class SaveCategoryUseCase @Inject constructor(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(category: Category): Result<Long> = runCatching {
        val name = category.name.trim()
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        validate(!repository.categoryNameExists(name, category.id), ValidationError.DUPLICATE_NAME)
        repository.saveCategory(category.copy(name = name))
    }
}

/** Adds or renames a storage place. Names must be non-blank and unique (ignoring case). */
class SaveStoragePlaceUseCase @Inject constructor(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(place: StoragePlace): Result<Long> = runCatching {
        val name = place.name.trim()
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        validate(!repository.placeNameExists(name, place.id), ValidationError.DUPLICATE_NAME)
        repository.savePlace(place.copy(name = name))
    }
}
