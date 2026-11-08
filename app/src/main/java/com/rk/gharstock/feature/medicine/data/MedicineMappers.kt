package com.rk.gharstock.feature.medicine.data

import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.relation.MedicineWithSchedule
import com.rk.gharstock.core.database.relation.MemberWithMedicinesRelation
import com.rk.gharstock.feature.medicine.domain.model.DoseLog
import com.rk.gharstock.feature.medicine.domain.model.DoseSchedule
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.MemberWithMedicines

internal fun FamilyMemberEntity.toModel() = FamilyMember(id = id, name = name, relation = relation)

internal fun FamilyMember.toEntity() = FamilyMemberEntity(id = id, name = name, relation = relation)

internal fun MemberWithMedicinesRelation.toModel() = MemberWithMedicines(
    member = member.toModel(),
    medicines = medicines.mapNotNull { it.toModel() },
)

/** A medicine is always saved together with its schedule, so a missing schedule is skipped. */
internal fun MedicineWithSchedule.toModel(): Medicine? = schedule?.let {
    Medicine(
        id = medicine.id,
        memberId = medicine.memberId,
        name = medicine.name,
        doseAmount = medicine.doseAmount,
        pillsLeft = medicine.pillsLeft,
        expiryDate = medicine.expiryDate,
        notes = medicine.notes,
        schedule = it.toModel(),
    )
}

internal fun Medicine.toEntity() = MedicineEntity(
    id = id,
    memberId = memberId,
    name = name,
    doseAmount = doseAmount,
    pillsLeft = pillsLeft,
    expiryDate = expiryDate,
    notes = notes,
)

private fun DoseScheduleEntity.toModel() = DoseSchedule(
    type = type,
    times = times,
    startTime = startTime,
    intervalHours = intervalHours,
    daysOfWeek = daysOfWeek,
    startDate = startDate,
    durationDays = durationDays,
)

internal fun DoseSchedule.toEntity(medicineId: Long) = DoseScheduleEntity(
    medicineId = medicineId,
    type = type,
    times = times.sorted(),
    startTime = startTime,
    intervalHours = intervalHours,
    daysOfWeek = daysOfWeek,
    startDate = startDate,
    durationDays = durationDays,
)

internal fun DoseLogEntity.toModel() = DoseLog(
    id = id,
    medicineId = medicineId,
    scheduledAt = scheduledAt,
    status = status,
    loggedAt = loggedAt,
)

internal fun DoseLog.toEntity() = DoseLogEntity(
    id = id,
    medicineId = medicineId,
    scheduledAt = scheduledAt,
    status = status,
    loggedAt = loggedAt,
)
