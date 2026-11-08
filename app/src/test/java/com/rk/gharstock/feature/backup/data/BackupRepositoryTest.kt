package com.rk.gharstock.feature.backup.data

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.fixedTimesSchedule
import com.rk.gharstock.core.database.medicineEntity
import com.rk.gharstock.core.database.shoppingItem
import com.rk.gharstock.core.database.stockItem
import com.rk.gharstock.feature.backup.domain.InvalidBackupException
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRepositoryTest : DatabaseTest() {

    private val repository by lazy { BackupRepositoryImpl(db.backupDao(), clock) }

    private suspend fun fillDatabase() {
        val dairy = db.categoryDao().observeAll().first().first { it.name == "Dairy" }.id
        val fridge = db.storagePlaceDao().observeAll().first().first { it.name == "Fridge" }.id
        val listId = db.shoppingListDao().insert(ShoppingListEntity(name = "Kiryana", createdAt = 5))
        db.shoppingItemDao().insert(shoppingItem(listId, name = "Sugar", bought = true, categoryId = dairy))
        db.stockItemDao().insert(stockItem(name = "Milk", expiryDate = today.plusDays(2), categoryId = dairy, placeId = fridge))
        val memberId = db.familyMemberDao().insert(FamilyMemberEntity(name = "Ammi", relation = "Mother"))
        val schedule = fixedTimesSchedule(LocalTime.of(8, 0), LocalTime.of(21, 30)).copy(daysOfWeek = setOf(DayOfWeek.MONDAY))
        val medicineId = db.medicineDao().save(medicineEntity(memberId).copy(expiryDate = today), schedule)
        db.doseLogDao().insert(DoseLogEntity(medicineId = medicineId, scheduledAt = 99, status = DoseStatus.SKIPPED, loggedAt = 100))
    }

    @Test
    fun `restore brings back exactly the exported data`() = runTest {
        fillDatabase()
        val before = db.backupDao().snapshot()
        val json = repository.createBackup()

        db.familyMemberDao().delete(before.members.single().id)
        db.shoppingListDao().delete(before.shoppingLists.single().id)
        db.stockItemDao().insert(stockItem(name = "Extra"))
        repository.restore(json)

        assertEquals(before, db.backupDao().snapshot())
    }

    @Test
    fun `summary counts the backup contents`() = runTest {
        fillDatabase()

        val summary = repository.readSummary(repository.createBackup())

        assertEquals(1, summary.stockItems)
        assertEquals(1, summary.shoppingItems)
        assertEquals(1, summary.medicines)
        assertEquals(1, summary.familyMembers)
        assertEquals(clock.millis(), summary.exportedAt)
    }

    @Test
    fun `invalid files are rejected and data is kept`() = runTest {
        fillDatabase()
        val before = db.backupDao().snapshot()

        assertThrows(InvalidBackupException::class.java) { repository.readSummary("not json") }
        assertThrows(InvalidBackupException::class.java) { repository.readSummary("""{"format":"other","exportedAt":1}""") }
        val brokenReference = """{"format":"ghar-stock-backup","version":1,"exportedAt":1,
            "shoppingItems":[{"id":1,"listId":42,"name":"x","quantity":1.0,"unit":"KG"}]}"""
        val error = runCatching { repository.restore(brokenReference) }.exceptionOrNull()
        assertTrue(error is InvalidBackupException)

        assertEquals(before, db.backupDao().snapshot())
    }
}
