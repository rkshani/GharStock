package com.rk.gharstock.feature.settings.domain.usecase

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.ValidationException
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.feature.settings.data.CatalogRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCatalogUseCasesTest : DatabaseTest() {

    private val repo by lazy { CatalogRepositoryImpl(db.categoryDao(), db.storagePlaceDao()) }

    private fun errorOf(result: Result<*>) = (result.exceptionOrNull() as ValidationException).error

    @Test
    fun `duplicate category names are rejected ignoring case`() = runTest {
        val save = SaveCategoryUseCase(repo)

        assertEquals(ValidationError.DUPLICATE_NAME, errorOf(save(Category(name = "dairy"))))
        assertEquals(ValidationError.BLANK_NAME, errorOf(save(Category(name = "  "))))
        assertTrue(save(Category(name = " Bakery ")).isSuccess)
        assertTrue(repo.observeCategories().first().any { it.name == "Bakery" })
    }

    @Test
    fun `renaming a place to its own name is allowed`() = runTest {
        val save = SaveStoragePlaceUseCase(repo)
        val fridge = repo.observePlaces().first().first { it.name == "Fridge" }

        assertTrue(save(fridge.copy(name = "fridge")).isSuccess)
        assertEquals(ValidationError.DUPLICATE_NAME, errorOf(save(StoragePlace(name = "Kitchen"))))
    }
}
