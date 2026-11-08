package com.rk.gharstock.feature.medicine.domain

import com.rk.gharstock.feature.medicine.domain.model.DoseLog
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.MemberWithMedicines
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface FamilyMemberRepository {
    fun observeMembers(): Flow<List<FamilyMember>>
    suspend fun saveMember(member: FamilyMember): Long

    /** Also deletes the member's medicines, schedules and dose history. */
    suspend fun deleteMember(id: Long)
}

interface MedicineRepository {
    fun observeMedicinesByMember(): Flow<List<MemberWithMedicines>>
    fun observeMedicine(id: Long): Flow<Medicine?>
    suspend fun getMedicine(id: Long): Medicine?
    suspend fun getAllMedicines(): List<Medicine>

    /** Medicines expiring on or before [date], including already expired ones. */
    fun observeExpiringBy(date: LocalDate): Flow<List<Medicine>>
    suspend fun saveMedicine(medicine: Medicine): Long
    suspend fun deleteMedicine(id: Long)
    suspend fun reducePills(medicineId: Long, amount: Double)

    fun observeDoseLogs(medicineId: Long): Flow<List<DoseLog>>

    /** Dose logs whose planned time is in [fromMillis, toMillis). */
    fun observeDoseLogsBetween(fromMillis: Long, toMillis: Long): Flow<List<DoseLog>>

    suspend fun isDoseLogged(medicineId: Long, scheduledAt: Long): Boolean

    /** Returns false when this dose was already logged. */
    suspend fun logDose(log: DoseLog): Boolean
}
