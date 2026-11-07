package com.rk.gharstock.core.database

import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicineDaoTest : DatabaseTest() {

    private val dao get() = db.medicineDao()

    private suspend fun addMember(name: String = "Ammi") =
        db.familyMemberDao().insert(FamilyMemberEntity(name = name, relation = "Mother"))

    @Test
    fun `save stores medicine with its schedule`() = runTest {
        val memberId = addMember()
        val id = dao.save(medicineEntity(memberId), fixedTimesSchedule(LocalTime.of(8, 0), LocalTime.of(20, 0)))

        val loaded = dao.getById(id)!!

        assertEquals("Panadol", loaded.medicine.name)
        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)), loaded.schedule?.times)
    }

    @Test
    fun `saving again updates medicine and replaces schedule`() = runTest {
        val memberId = addMember()
        val id = dao.save(medicineEntity(memberId), fixedTimesSchedule(LocalTime.of(8, 0)))

        dao.save(medicineEntity(memberId).copy(id = id, name = "Brufen"), fixedTimesSchedule(LocalTime.of(9, 0)))

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("Brufen", all.single().medicine.name)
        assertEquals(listOf(LocalTime.of(9, 0)), all.single().schedule?.times)
    }

    @Test
    fun `reduce pills never goes below zero`() = runTest {
        val id = dao.save(medicineEntity(addMember(), pillsLeft = 3.0), fixedTimesSchedule(LocalTime.of(8, 0)))

        dao.reducePills(id, 2.0)
        assertEquals(1.0, dao.getById(id)!!.medicine.pillsLeft, 0.0)
        dao.reducePills(id, 2.0)
        assertEquals(0.0, dao.getById(id)!!.medicine.pillsLeft, 0.0)
    }

    @Test
    fun `medicines are grouped under their member`() = runTest {
        val ammi = addMember("Ammi")
        addMember("Abbu")
        dao.save(medicineEntity(ammi), fixedTimesSchedule(LocalTime.of(8, 0)))

        val groups = dao.observeMembersWithMedicines().first()

        assertEquals(listOf("Abbu", "Ammi"), groups.map { it.member.name })
        assertTrue(groups[0].medicines.isEmpty())
        assertEquals(1, groups[1].medicines.size)
    }

    @Test
    fun `deleting a member deletes medicines, schedules and logs`() = runTest {
        val memberId = addMember()
        val id = dao.save(medicineEntity(memberId), fixedTimesSchedule(LocalTime.of(8, 0)))
        db.doseLogDao().insert(DoseLogEntity(medicineId = id, scheduledAt = 1, status = DoseStatus.TAKEN, loggedAt = 1))

        db.familyMemberDao().delete(memberId)

        assertNull(dao.getById(id))
        assertTrue(db.doseLogDao().observeForMedicine(id).first().isEmpty())
    }

    @Test
    fun `logging the same dose twice is ignored`() = runTest {
        val id = dao.save(medicineEntity(addMember()), fixedTimesSchedule(LocalTime.of(8, 0)))
        val log = DoseLogEntity(medicineId = id, scheduledAt = 100, status = DoseStatus.TAKEN, loggedAt = 1)

        val first = db.doseLogDao().insert(log)
        val second = db.doseLogDao().insert(log.copy(status = DoseStatus.SKIPPED))

        assertTrue(first > 0)
        assertEquals(-1L, second)
        assertEquals(DoseStatus.TAKEN, db.doseLogDao().observeForMedicine(id).first().single().status)
    }
}
