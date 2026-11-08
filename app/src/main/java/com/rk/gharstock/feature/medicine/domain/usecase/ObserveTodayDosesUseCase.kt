package com.rk.gharstock.feature.medicine.domain.usecase

import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.dosesOn
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** One planned dose for today, with its Taken/Skipped status if already logged. */
data class TodayDose(
    val medicine: Medicine,
    val memberName: String,
    val time: LocalTime,
    /** Epoch millis of the planned dose, used as the dose log key. */
    val scheduledAt: Long,
    val status: DoseStatus?,
)

/** All doses planned for [date] across the family, in time order. */
class ObserveTodayDosesUseCase @Inject constructor(
    private val repository: MedicineRepository,
    private val clock: Clock,
) {
    operator fun invoke(date: LocalDate): Flow<List<TodayDose>> {
        val zone = clock.zone
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return combine(
            repository.observeMedicinesByMember(),
            repository.observeDoseLogsBetween(dayStart, dayEnd),
        ) { groups, logs ->
            val statusByDose = logs.associate { (it.medicineId to it.scheduledAt) to it.status }
            groups.flatMap { group ->
                group.medicines.flatMap { medicine ->
                    medicine.schedule.dosesOn(date).map { time ->
                        val scheduledAt = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
                        TodayDose(medicine, group.member.name, time, scheduledAt, statusByDose[medicine.id to scheduledAt])
                    }
                }
            }.sortedWith(compareBy({ it.time }, { it.medicine.name }))
        }
    }
}
