package com.rk.gharstock.feature.medicine.domain.usecase

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.ValidationException
import com.rk.gharstock.feature.medicine.data.FamilyMemberRepositoryImpl
import com.rk.gharstock.feature.medicine.data.MedicineRepositoryImpl
import com.rk.gharstock.feature.medicine.domain.model.DoseSchedule
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicineUseCasesTest : DatabaseTest() {

    private val memberRepo by lazy { FamilyMemberRepositoryImpl(db.familyMemberDao()) }
    private val medicineRepo by lazy { MedicineRepositoryImpl(db.medicineDao(), db.doseLogDao()) }

    private val saveMember by lazy { SaveFamilyMemberUseCase(memberRepo) }
    private val saveMedicine by lazy { SaveMedicineUseCase(medicineRepo) }
    private val logDose by lazy { LogDoseUseCase(medicineRepo, transaction, clock) }

    private fun errorOf(result: Result<*>) = (result.exceptionOrNull() as ValidationException).error

    private suspend fun newMedicine(pillsLeft: Double = 10.0): Medicine {
        val memberId = saveMember(FamilyMember(name = "Ammi")).getOrThrow()
        return Medicine(
            memberId = memberId,
            name = "Panadol",
            doseAmount = 2.0,
            pillsLeft = pillsLeft,
            schedule = DoseSchedule(ScheduleType.FIXED_TIMES, times = listOf(LocalTime.of(8, 0)), startDate = today),
        )
    }

    @Test
    fun `member relation is optional and blank becomes null`() = runTest {
        saveMember(FamilyMember(name = " Abbu ", relation = "  ")).getOrThrow()

        val member = memberRepo.observeMembers().first().single()
        assertEquals("Abbu", member.name)
        assertNull(member.relation)
        assertEquals(ValidationError.BLANK_NAME, errorOf(saveMember(FamilyMember(name = ""))))
    }

    @Test
    fun `medicine validation rejects bad dose and schedule`() = runTest {
        val medicine = newMedicine()

        assertEquals(ValidationError.INVALID_DOSE, errorOf(saveMedicine(medicine.copy(doseAmount = 0.0))))
        assertEquals(
            ValidationError.INVALID_SCHEDULE,
            errorOf(saveMedicine(medicine.copy(schedule = medicine.schedule.copy(times = emptyList())))),
        )
        assertTrue(saveMedicine(medicine).isSuccess)
    }

    @Test
    fun `taken dose reduces pills and skipped dose does not`() = runTest {
        val id = saveMedicine(newMedicine(pillsLeft = 10.0)).getOrThrow()

        assertTrue(logDose(id, scheduledAt = 1_000, status = DoseStatus.TAKEN))
        assertTrue(logDose(id, scheduledAt = 2_000, status = DoseStatus.SKIPPED))

        assertEquals(8.0, medicineRepo.getMedicine(id)!!.pillsLeft, 0.0)
        assertEquals(
            listOf(DoseStatus.SKIPPED, DoseStatus.TAKEN),
            medicineRepo.observeDoseLogs(id).first().map { it.status },
        )
    }

    @Test
    fun `same dose logged twice reduces pills only once`() = runTest {
        val id = saveMedicine(newMedicine(pillsLeft = 10.0)).getOrThrow()

        assertTrue(logDose(id, scheduledAt = 1_000, status = DoseStatus.TAKEN))
        assertFalse(logDose(id, scheduledAt = 1_000, status = DoseStatus.TAKEN))

        assertEquals(8.0, medicineRepo.getMedicine(id)!!.pillsLeft, 0.0)
    }

    @Test
    fun `logging for a missing medicine returns false`() = runTest {
        assertFalse(logDose(medicineId = 99, scheduledAt = 1_000, status = DoseStatus.TAKEN))
    }
}
