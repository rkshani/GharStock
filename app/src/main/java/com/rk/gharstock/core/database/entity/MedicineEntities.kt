package com.rk.gharstock.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relation: String?,
)

@Entity(
    tableName = "medicines",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("member_id")],
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "member_id") val memberId: Long,
    val name: String,
    @ColumnInfo(name = "dose_amount") val doseAmount: Double,
    @ColumnInfo(name = "pills_left") val pillsLeft: Double,
    @ColumnInfo(name = "expiry_date") val expiryDate: LocalDate?,
    val notes: String?,
)

/** One schedule per medicine. Which columns are used depends on [type]. */
@Entity(
    tableName = "dose_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicine_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["medicine_id"], unique = true)],
)
data class DoseScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medicine_id") val medicineId: Long,
    val type: ScheduleType,
    /** Dose times for FIXED_TIMES, DAYS_OF_WEEK and COURSE. */
    val times: List<LocalTime>,
    /** First dose time for EVERY_X_HOURS. */
    @ColumnInfo(name = "start_time") val startTime: LocalTime?,
    @ColumnInfo(name = "interval_hours") val intervalHours: Int?,
    @ColumnInfo(name = "days_of_week") val daysOfWeek: Set<DayOfWeek>,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    /** Required for COURSE, optional end for the other types. */
    @ColumnInfo(name = "duration_days") val durationDays: Int?,
)

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicine_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["medicine_id", "scheduled_at"], unique = true)],
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medicine_id") val medicineId: Long,
    /** Epoch millis of the planned dose time. */
    @ColumnInfo(name = "scheduled_at") val scheduledAt: Long,
    val status: DoseStatus,
    /** Epoch millis when the user tapped Taken/Skip. */
    @ColumnInfo(name = "logged_at") val loggedAt: Long,
)
