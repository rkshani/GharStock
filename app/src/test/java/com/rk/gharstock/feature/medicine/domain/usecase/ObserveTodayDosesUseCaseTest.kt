package com.rk.gharstock.feature.medicine.domain.usecase

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.fixedTimesSchedule
import com.rk.gharstock.core.database.medicineEntity
import com.rk.gharstock.feature.medicine.data.MedicineRepositoryImpl
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveTodayDosesUseCaseTest : DatabaseTest() {

    @Test
    fun `lists today's doses in time order with logged status`() = runTest {
        val memberId = db.familyMemberDao().insert(FamilyMemberEntity(name = "Ammi", relation = null))
        val id = db.medicineDao().save(
            medicineEntity(memberId),
            fixedTimesSchedule(LocalTime.of(20, 0), LocalTime.of(8, 0)).copy(startDate = today),
        )
        val morning = today.atTime(8, 0).atZone(clock.zone).toInstant().toEpochMilli()
        db.doseLogDao().insert(DoseLogEntity(medicineId = id, scheduledAt = morning, status = DoseStatus.TAKEN, loggedAt = 0))
        val useCase = ObserveTodayDosesUseCase(MedicineRepositoryImpl(db.medicineDao(), db.doseLogDao()), clock)

        val doses = useCase(today).first()

        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)), doses.map { it.time })
        assertEquals(listOf(DoseStatus.TAKEN, null), doses.map { it.status })
        assertEquals("Ammi", doses.first().memberName)
        assertEquals(morning, doses.first().scheduledAt)
    }
}
