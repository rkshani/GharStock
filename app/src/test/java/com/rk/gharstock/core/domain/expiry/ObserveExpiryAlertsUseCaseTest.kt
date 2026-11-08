package com.rk.gharstock.core.domain.expiry

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.fixedTimesSchedule
import com.rk.gharstock.core.database.medicineEntity
import com.rk.gharstock.core.database.stockItem
import com.rk.gharstock.core.model.ExpiryStatus
import com.rk.gharstock.feature.medicine.data.MedicineRepositoryImpl
import com.rk.gharstock.feature.stock.data.StockRepositoryImpl
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveExpiryAlertsUseCaseTest : DatabaseTest() {

    @Test
    fun `combines expired and soon items from stock and medicines`() = runTest {
        val stockDao = db.stockItemDao()
        stockDao.insert(stockItem(name = "Milk", expiryDate = today.plusDays(1)))
        stockDao.insert(stockItem(name = "Bread", expiryDate = today.minusDays(2)))
        stockDao.insert(stockItem(name = "Rice", expiryDate = today.plusDays(20)))
        val memberId = db.familyMemberDao().insert(FamilyMemberEntity(name = "Ammi", relation = null))
        db.medicineDao().save(
            medicineEntity(memberId).copy(expiryDate = today.plusDays(3)),
            fixedTimesSchedule(LocalTime.of(8, 0)),
        )
        val useCase = ObserveExpiryAlertsUseCase(
            StockRepositoryImpl(stockDao),
            MedicineRepositoryImpl(db.medicineDao(), db.doseLogDao()),
        )

        val alerts = useCase(today).first()

        assertEquals(listOf("Bread", "Milk", "Panadol"), alerts.map { it.name })
        assertEquals(
            listOf(ExpiryStatus.EXPIRED, ExpiryStatus.EXPIRING_SOON, ExpiryStatus.EXPIRING_SOON),
            alerts.map { it.status },
        )
        assertEquals(ExpirySource.MEDICINE, alerts.last().source)
    }
}
