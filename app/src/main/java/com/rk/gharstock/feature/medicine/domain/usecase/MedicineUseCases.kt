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
import com.rk.gharstock.feature.medicine.domain.model.isLowOnPills
import com.rk.gharstock.feature.medicine.domain.model.isValid
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import com.rk.gharstock.feature.medicine.domain.reminder.MedicineNotifier
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

/** Validates and saves a medicine, then schedules its next dose reminder. */
class SaveMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository,
    private val reminders: DoseReminderScheduler,
) {
    suspend operator fun invoke(medicine: Medicine): Result<Long> = runCatching {
        val name = medicine.name.trim()
        validate(medicine.memberId > 0, ValidationError.MISSING_MEMBER)
        validate(name.isNotEmpty(), ValidationError.BLANK_NAME)
        validate(medicine.doseAmount > 0, ValidationError.INVALID_DOSE)
        validate(medicine.pillsLeft >= 0, ValidationError.INVALID_QUANTITY)
        validate(medicine.schedule.isValid(), ValidationError.INVALID_SCHEDULE)
        val notes = medicine.notes?.trim()?.takeIf { it.isNotEmpty() }
        val id = repository.saveMedicine(medicine.copy(name = name, notes = notes))
        repository.getMedicine(id)?.let(reminders::schedule)
        id
    }
}

class DeleteMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository,
    private val reminders: DoseReminderScheduler,
    private val notifier: MedicineNotifier,
) {
    suspend operator fun invoke(medicineId: Long) {
        reminders.cancel(medicineId)
        notifier.cancelDoseNotification(medicineId)
        repository.deleteMedicine(medicineId)
    }
}

/** Deletes a family member together with their medicines, reminders and dose history. */
class DeleteFamilyMemberUseCase @Inject constructor(
    private val memberRepository: FamilyMemberRepository,
    private val medicineRepository: MedicineRepository,
    private val reminders: DoseReminderScheduler,
    private val notifier: MedicineNotifier,
) {
    suspend operator fun invoke(memberId: Long) {
        medicineRepository.getAllMedicines()
            .filter { it.memberId == memberId }
            .forEach {
                reminders.cancel(it.id)
                notifier.cancelDoseNotification(it.id)
            }
        memberRepository.deleteMember(memberId)
    }
}

data class DoseLogResult(
    /** False when the dose was already logged or the medicine no longer exists. */
    val recorded: Boolean,
    /** True when this Taken dose made the medicine run low on pills. */
    val becameLowOnPills: Boolean = false,
)

/**
 * Records Taken/Skip for one planned dose. Taken reduces the pill count by the dose amount.
 * Logging the same dose twice is ignored. Clears the dose notification and alerts once
 * when pills drop to 3 days or fewer.
 */
class LogDoseUseCase @Inject constructor(
    private val repository: MedicineRepository,
    private val transaction: TransactionRunner,
    private val notifier: MedicineNotifier,
    private val clock: Clock,
) {
    suspend operator fun invoke(medicineId: Long, scheduledAt: Long, status: DoseStatus): DoseLogResult {
        val result = transaction {
            val before = repository.getMedicine(medicineId) ?: return@transaction DoseLogResult(recorded = false)
            val log = DoseLog(medicineId = medicineId, scheduledAt = scheduledAt, status = status, loggedAt = clock.millis())
            val recorded = repository.logDose(log)
            if (!recorded || status != DoseStatus.TAKEN) return@transaction DoseLogResult(recorded)
            repository.reducePills(medicineId, before.doseAmount)
            val after = repository.getMedicine(medicineId)
            DoseLogResult(recorded = true, becameLowOnPills = !before.isLowOnPills() && after?.isLowOnPills() == true)
        }
        notifier.cancelDoseNotification(medicineId)
        if (result.becameLowOnPills) repository.getMedicine(medicineId)?.let(notifier::showLowPills)
        return result
    }
}
