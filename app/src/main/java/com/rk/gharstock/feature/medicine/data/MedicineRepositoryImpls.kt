package com.rk.gharstock.feature.medicine.data

import com.rk.gharstock.core.database.dao.DoseLogDao
import com.rk.gharstock.core.database.dao.FamilyMemberDao
import com.rk.gharstock.core.database.dao.MedicineDao
import com.rk.gharstock.feature.medicine.domain.FamilyMemberRepository
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.DoseLog
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.MemberWithMedicines
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FamilyMemberRepositoryImpl @Inject constructor(
    private val dao: FamilyMemberDao,
) : FamilyMemberRepository {

    override fun observeMembers(): Flow<List<FamilyMember>> =
        dao.observeAll().map { members -> members.map { it.toModel() } }

    override suspend fun saveMember(member: FamilyMember): Long =
        if (member.id == 0L) {
            dao.insert(member.toEntity())
        } else {
            dao.update(member.toEntity())
            member.id
        }

    override suspend fun deleteMember(id: Long) = dao.delete(id)
}

class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao,
    private val doseLogDao: DoseLogDao,
) : MedicineRepository {

    override fun observeMedicinesByMember(): Flow<List<MemberWithMedicines>> =
        medicineDao.observeMembersWithMedicines().map { members -> members.map { it.toModel() } }

    override fun observeMedicine(id: Long): Flow<Medicine?> =
        medicineDao.observeById(id).map { it?.toModel() }

    override suspend fun getMedicine(id: Long): Medicine? = medicineDao.getById(id)?.toModel()

    override suspend fun getAllMedicines(): List<Medicine> = medicineDao.getAll().mapNotNull { it.toModel() }

    override fun observeExpiringBy(date: LocalDate): Flow<List<Medicine>> =
        medicineDao.observeExpiringBy(date).map { list -> list.mapNotNull { it.toModel() } }

    override suspend fun saveMedicine(medicine: Medicine): Long =
        medicineDao.save(medicine.toEntity(), medicine.schedule.toEntity(medicine.id))

    override suspend fun deleteMedicine(id: Long) = medicineDao.delete(id)

    override suspend fun reducePills(medicineId: Long, amount: Double) =
        medicineDao.reducePills(medicineId, amount)

    override fun observeDoseLogs(medicineId: Long): Flow<List<DoseLog>> =
        doseLogDao.observeForMedicine(medicineId).map { logs -> logs.map { it.toModel() } }

    override fun observeDoseLogsBetween(fromMillis: Long, toMillis: Long): Flow<List<DoseLog>> =
        doseLogDao.observeBetween(fromMillis, toMillis).map { logs -> logs.map { it.toModel() } }

    override suspend fun isDoseLogged(medicineId: Long, scheduledAt: Long): Boolean =
        doseLogDao.isLogged(medicineId, scheduledAt)

    override suspend fun logDose(log: DoseLog): Boolean = doseLogDao.insert(log.toEntity()) != -1L
}
