package com.rk.gharstock.feature.backup.domain

import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import com.rk.gharstock.feature.medicine.domain.reminder.MedicineNotifier
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
    private val documents: DocumentStore,
) {
    suspend operator fun invoke(uri: String): Result<Unit> = runCatching {
        documents.write(uri, repository.createBackup())
    }
}

/** A backup file that was read and checked, waiting for the user to confirm the restore. */
data class PendingRestore(val json: String, val summary: BackupSummary)

class ReadBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
    private val documents: DocumentStore,
) {
    suspend operator fun invoke(uri: String): Result<PendingRestore> = runCatching {
        val json = documents.read(uri)
        PendingRestore(json, repository.readSummary(json))
    }
}

/** Replaces all data with the backup and rebuilds medicine reminders for the restored medicines. */
class RestoreBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
    private val medicineRepository: MedicineRepository,
    private val reminders: DoseReminderScheduler,
    private val notifier: MedicineNotifier,
) {
    suspend operator fun invoke(json: String): Result<Unit> = runCatching {
        medicineRepository.getAllMedicines().forEach {
            reminders.cancel(it.id)
            notifier.cancelDoseNotification(it.id)
        }
        repository.restore(json)
        reminders.rescheduleAll()
    }
}
