package com.rk.gharstock.feature.backup.domain

/** What a backup file contains, shown before the user confirms a restore. */
data class BackupSummary(
    val exportedAt: Long,
    val stockItems: Int,
    val shoppingItems: Int,
    val medicines: Int,
    val familyMembers: Int,
)

/** The file is not a Ghar Stock backup or is damaged. */
class InvalidBackupException(cause: Throwable? = null) : Exception("Invalid backup file", cause)

interface BackupRepository {
    /** All app data as backup JSON. */
    suspend fun createBackup(): String

    /** Parses [json] without changing data. Throws [InvalidBackupException]. */
    fun readSummary(json: String): BackupSummary

    /** Replaces all app data with the backup. Throws [InvalidBackupException]. */
    suspend fun restore(json: String)
}

/** Reads and writes text documents chosen by the user (Storage Access Framework URIs). */
interface DocumentStore {
    suspend fun read(uri: String): String

    suspend fun write(uri: String, text: String)
}
