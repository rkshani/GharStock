package com.rk.gharstock.core.domain.expiry

import com.rk.gharstock.core.model.EXPIRY_WARNING_DAYS
import com.rk.gharstock.core.model.ExpiryStatus
import com.rk.gharstock.core.model.expiryStatusOf
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.stock.domain.StockRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

enum class ExpirySource { STOCK, MEDICINE }

/** A stock item or medicine that is expired or expires within [EXPIRY_WARNING_DAYS] days. */
data class ExpiryAlert(
    val id: Long,
    val name: String,
    val expiryDate: LocalDate,
    val status: ExpiryStatus,
    val source: ExpirySource,
)

/** Expired and soon-to-expire stock items and medicines, soonest first. */
class ObserveExpiryAlertsUseCase @Inject constructor(
    private val stockRepository: StockRepository,
    private val medicineRepository: MedicineRepository,
) {
    operator fun invoke(today: LocalDate): Flow<List<ExpiryAlert>> {
        val limit = today.plusDays(EXPIRY_WARNING_DAYS)
        return combine(
            stockRepository.observeExpiringBy(limit),
            medicineRepository.observeExpiringBy(limit),
        ) { stock, medicines ->
            val stockAlerts = stock.mapNotNull { item ->
                item.expiryDate?.let { ExpiryAlert(item.id, item.name, it, expiryStatusOf(it, today), ExpirySource.STOCK) }
            }
            val medicineAlerts = medicines.mapNotNull { medicine ->
                medicine.expiryDate?.let {
                    ExpiryAlert(medicine.id, medicine.name, it, expiryStatusOf(it, today), ExpirySource.MEDICINE)
                }
            }
            (stockAlerts + medicineAlerts).sortedWith(compareBy({ it.expiryDate }, { it.name }))
        }
    }
}
