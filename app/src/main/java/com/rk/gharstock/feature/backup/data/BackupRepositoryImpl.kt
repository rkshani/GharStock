package com.rk.gharstock.feature.backup.data

import android.database.SQLException
import com.rk.gharstock.core.database.dao.BackupDao
import com.rk.gharstock.feature.backup.domain.BackupRepository
import com.rk.gharstock.feature.backup.domain.BackupSummary
import com.rk.gharstock.feature.backup.domain.InvalidBackupException
import java.time.Clock
import javax.inject.Inject
import kotlinx.serialization.json.Json

class BackupRepositoryImpl @Inject constructor(
    private val dao: BackupDao,
    private val clock: Clock,
) : BackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun createBackup(): String =
        json.encodeToString(BackupFile.serializer(), dao.snapshot().toBackupFile(exportedAt = clock.millis()))

    override fun readSummary(json: String): BackupSummary {
        val file = parse(json)
        return BackupSummary(
            exportedAt = file.exportedAt,
            stockItems = file.stockItems.size,
            shoppingItems = file.shoppingItems.size,
            medicines = file.medicines.size,
            familyMembers = file.familyMembers.size,
        )
    }

    override suspend fun restore(json: String) {
        val snapshot = try {
            parse(json).toSnapshot()
        } catch (e: RuntimeException) {
            throw InvalidBackupException(e)
        }
        try {
            dao.replaceAll(snapshot)
        } catch (e: SQLException) {
            // e.g. rows pointing to missing parents; the transaction is rolled back.
            throw InvalidBackupException(e)
        }
    }

    private fun parse(text: String): BackupFile {
        val file = try {
            json.decodeFromString(BackupFile.serializer(), text)
        } catch (e: RuntimeException) {
            throw InvalidBackupException(e)
        }
        if (file.format != BackupFile.FORMAT || file.version > BackupFile.VERSION) throw InvalidBackupException()
        return file
    }
}
