package com.rk.gharstock.feature.backup.data

import com.rk.gharstock.core.database.dao.DatabaseSnapshot
import com.rk.gharstock.core.database.entity.CategoryEntity
import com.rk.gharstock.core.database.entity.DoseLogEntity
import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.FamilyMemberEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.ShoppingListEntity
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.database.entity.StoragePlaceEntity
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable

/**
 * Backup file format. Dates are ISO strings (2026-11-08), times are HH:mm, enums by name,
 * so the file stays readable and independent of the database layout.
 */
@Serializable
data class BackupFile(
    val format: String = FORMAT,
    val version: Int = VERSION,
    val exportedAt: Long,
    val categories: List<NamedDto> = emptyList(),
    val storagePlaces: List<NamedDto> = emptyList(),
    val shoppingLists: List<ShoppingListDto> = emptyList(),
    val shoppingItems: List<ShoppingItemDto> = emptyList(),
    val stockItems: List<StockItemDto> = emptyList(),
    val familyMembers: List<FamilyMemberDto> = emptyList(),
    val medicines: List<MedicineDto> = emptyList(),
    val doseSchedules: List<DoseScheduleDto> = emptyList(),
    val doseLogs: List<DoseLogDto> = emptyList(),
) {
    companion object {
        const val FORMAT = "ghar-stock-backup"
        const val VERSION = 1
    }
}

@Serializable
data class NamedDto(val id: Long, val name: String)

@Serializable
data class ShoppingListDto(val id: Long, val name: String, val createdAt: Long)

@Serializable
data class ShoppingItemDto(
    val id: Long,
    val listId: Long,
    val name: String,
    val quantity: Double,
    val unit: String,
    val categoryId: Long? = null,
    val price: Double? = null,
    val isBought: Boolean = false,
    val createdAt: Long = 0,
)

@Serializable
data class StockItemDto(
    val id: Long,
    val name: String,
    val quantity: Double,
    val unit: String,
    val categoryId: Long? = null,
    val placeId: Long? = null,
    val price: Double? = null,
    val purchaseDate: String,
    val expiryDate: String? = null,
    val createdAt: Long = 0,
)

@Serializable
data class FamilyMemberDto(val id: Long, val name: String, val relation: String? = null)

@Serializable
data class MedicineDto(
    val id: Long,
    val memberId: Long,
    val name: String,
    val doseAmount: Double,
    val pillsLeft: Double,
    val expiryDate: String? = null,
    val notes: String? = null,
)

@Serializable
data class DoseScheduleDto(
    val id: Long,
    val medicineId: Long,
    val type: String,
    val times: List<String> = emptyList(),
    val startTime: String? = null,
    val intervalHours: Int? = null,
    val daysOfWeek: List<String> = emptyList(),
    val startDate: String,
    val durationDays: Int? = null,
)

@Serializable
data class DoseLogDto(
    val id: Long,
    val medicineId: Long,
    val scheduledAt: Long,
    val status: String,
    val loggedAt: Long,
)

fun DatabaseSnapshot.toBackupFile(exportedAt: Long) = BackupFile(
    exportedAt = exportedAt,
    categories = categories.map { NamedDto(it.id, it.name) },
    storagePlaces = places.map { NamedDto(it.id, it.name) },
    shoppingLists = shoppingLists.map { ShoppingListDto(it.id, it.name, it.createdAt) },
    shoppingItems = shoppingItems.map {
        ShoppingItemDto(it.id, it.listId, it.name, it.quantity, it.unit.name, it.categoryId, it.price, it.isBought, it.createdAt)
    },
    stockItems = stockItems.map {
        StockItemDto(
            it.id, it.name, it.quantity, it.unit.name, it.categoryId, it.placeId, it.price,
            it.purchaseDate.toString(), it.expiryDate?.toString(), it.createdAt,
        )
    },
    familyMembers = members.map { FamilyMemberDto(it.id, it.name, it.relation) },
    medicines = medicines.map {
        MedicineDto(it.id, it.memberId, it.name, it.doseAmount, it.pillsLeft, it.expiryDate?.toString(), it.notes)
    },
    doseSchedules = schedules.map {
        DoseScheduleDto(
            id = it.id,
            medicineId = it.medicineId,
            type = it.type.name,
            times = it.times.map(LocalTime::toString),
            startTime = it.startTime?.toString(),
            intervalHours = it.intervalHours,
            daysOfWeek = it.daysOfWeek.map(DayOfWeek::name),
            startDate = it.startDate.toString(),
            durationDays = it.durationDays,
        )
    },
    doseLogs = doseLogs.map { DoseLogDto(it.id, it.medicineId, it.scheduledAt, it.status.name, it.loggedAt) },
)

/** Converts back to database rows. Throws [IllegalArgumentException] for malformed values. */
fun BackupFile.toSnapshot() = DatabaseSnapshot(
    categories = categories.map { CategoryEntity(it.id, it.name) },
    places = storagePlaces.map { StoragePlaceEntity(it.id, it.name) },
    shoppingLists = shoppingLists.map { ShoppingListEntity(it.id, it.name, it.createdAt) },
    shoppingItems = shoppingItems.map {
        ShoppingItemEntity(
            it.id, it.listId, it.name, it.quantity, MeasureUnit.valueOf(it.unit),
            it.categoryId, it.price, it.isBought, it.createdAt,
        )
    },
    stockItems = stockItems.map {
        StockItemEntity(
            it.id, it.name, it.quantity, MeasureUnit.valueOf(it.unit), it.categoryId, it.placeId, it.price,
            LocalDate.parse(it.purchaseDate), it.expiryDate?.let(LocalDate::parse), it.createdAt,
        )
    },
    members = familyMembers.map { FamilyMemberEntity(it.id, it.name, it.relation) },
    medicines = medicines.map {
        MedicineEntity(it.id, it.memberId, it.name, it.doseAmount, it.pillsLeft, it.expiryDate?.let(LocalDate::parse), it.notes)
    },
    schedules = doseSchedules.map {
        DoseScheduleEntity(
            id = it.id,
            medicineId = it.medicineId,
            type = ScheduleType.valueOf(it.type),
            times = it.times.map(LocalTime::parse),
            startTime = it.startTime?.let(LocalTime::parse),
            intervalHours = it.intervalHours,
            daysOfWeek = it.daysOfWeek.map(DayOfWeek::valueOf).toSet(),
            startDate = LocalDate.parse(it.startDate),
            durationDays = it.durationDays,
        )
    },
    doseLogs = doseLogs.map { DoseLogEntity(it.id, it.medicineId, it.scheduledAt, DoseStatus.valueOf(it.status), it.loggedAt) },
)
