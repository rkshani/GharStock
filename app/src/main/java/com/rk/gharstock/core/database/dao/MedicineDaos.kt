package com.rk.gharstock.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.relation.MedicineWithSchedule
import com.rk.gharstock.core.database.relation.MemberWithMedicinesRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {

    @Query("SELECT * FROM family_members ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<FamilyMemberEntity>>

    @Insert
    suspend fun insert(member: FamilyMemberEntity): Long

    @Update
    suspend fun update(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
abstract class MedicineDao {

    @Transaction
    @Query("SELECT * FROM family_members ORDER BY name COLLATE NOCASE")
    abstract fun observeMembersWithMedicines(): Flow<List<MemberWithMedicinesRelation>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    abstract fun observeById(id: Long): Flow<MedicineWithSchedule?>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    abstract suspend fun getById(id: Long): MedicineWithSchedule?

    @Transaction
    @Query("SELECT * FROM medicines")
    abstract suspend fun getAll(): List<MedicineWithSchedule>

    /** Inserts or updates the medicine and replaces its schedule. Returns the medicine id. */
    @Transaction
    open suspend fun save(medicine: MedicineEntity, schedule: DoseScheduleEntity): Long {
        val id = if (medicine.id == 0L) insertMedicine(medicine) else medicine.id.also { updateMedicine(medicine) }
        deleteSchedule(id)
        insertSchedule(schedule.copy(id = 0, medicineId = id))
        return id
    }

    @Query("UPDATE medicines SET pills_left = MAX(pills_left - :amount, 0) WHERE id = :id")
    abstract suspend fun reducePills(id: Long, amount: Double)

    @Query("DELETE FROM medicines WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Insert
    protected abstract suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    protected abstract suspend fun updateMedicine(medicine: MedicineEntity)

    @Insert
    protected abstract suspend fun insertSchedule(schedule: DoseScheduleEntity)

    @Query("DELETE FROM dose_schedules WHERE medicine_id = :medicineId")
    protected abstract suspend fun deleteSchedule(medicineId: Long)
}

@Dao
interface DoseLogDao {

    @Query("SELECT * FROM dose_logs WHERE medicine_id = :medicineId ORDER BY scheduled_at DESC")
    fun observeForMedicine(medicineId: Long): Flow<List<DoseLogEntity>>

    /** Returns -1 when this dose was already logged. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: DoseLogEntity): Long
}
