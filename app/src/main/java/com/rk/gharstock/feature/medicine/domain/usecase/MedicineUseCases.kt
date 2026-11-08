package com.rk.gharstock.feature.medicine.domain.usecase

import com.rk.gharstock.core.domain.TransactionRunner
import com.rk.gharstock.core.domain.ValidationError
import com.rk.gharstock.core.domain.validate
import com.rk.gharstock.feature.medicine.domain.FamilyMemberRepository
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.DoseLog
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.isValid
import java.time.Clock
import javax.inject.Inject

class SaveFamilyMemberUseCase @Inject constructor(
    private val repository: FamilyMemberRepository,
) {
    suspend operator fun invoke(member: FamilyMember): Result<Long> = runCatching {
        val name = member.name.trim()
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        val relation = member.relation?.trim()?.takeIf { it.isNotEmpty() }
        repository.saveMember(member.copy(name = name, relation = relation))
    }
}

class SaveMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository,
) {
    suspend operator fun invoke(medicine: Medicine): Result<Long> = runCatching {
        val name = medicine.name.trim()
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        validate(medicine.doseAmount > 0, ValidationError.INVALID_DOSE)
        validate(medicine.pillsLeft >= 0, ValidationError.INVALID_QUANTITY)
        validate(medicine.schedule.isValid(), ValidationError.INVALID_SCHEDULE)
        val notes = medicine.notes?.trim()?.takeIf { it.isNotEmpty() }
        repository.saveMedicine(medicine.copy(name = name, notes = notes))
    }
}

/**
 * Records Taken/Skip for one planned dose. Taken reduces the pill count by the dose amount.
 * Logging the same dose twice is ignored. Returns true when the dose was recorded.
 */
class LogDoseUseCase @Inject constructor(
    private val repository: MedicineRepository,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) {
    suspend operator fun invoke(medicineId: Long, scheduledAt: Long, status: DoseStatus): Boolean = transaction {
        val medicine = repository.getMedicine(medicineId) ?: return@transaction false
        val log = DoseLog(
            medicineId = medicineId,
            scheduledAt = scheduledAt,
            status = status,
            loggedAt = clock.millis(),
        )
        val recorded = repository.logDose(log)
        if (recorded && status == DoseStatus.TAKEN) {
            repository.reducePills(medicineId, medicine.doseAmount)
        }
        recorded
    }
}

/** Deletes a family member together with their medicines and dose history. */
class DeleteFamilyMemberUseCase @Inject constructor(
    private val repository: FamilyMemberRepository,
) {
    suspend operator fun invoke(memberId: Long) = repository.deleteMember(memberId)
}
