package com.rk.gharstock.feature.backup.domain

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

/** Replaces all data with the backup. */
class RestoreBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(json: String): Result<Unit> = runCatching { repository.restore(json) }
}
