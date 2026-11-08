package com.rk.gharstock.feature.report.domain

import com.rk.gharstock.core.database.DatabaseTest
import com.rk.gharstock.core.database.stockItem
import com.rk.gharstock.feature.report.data.ReportRepositoryImpl
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveMonthlyReportUseCaseTest : DatabaseTest() {

    @Test
    fun `sums prices of the month by category, highest first`() = runTest {
        val dao = db.stockItemDao()
        val dairy = db.categoryDao().observeAll().first().first { it.name == "Dairy" }.id
        val grains = db.categoryDao().observeAll().first().first { it.name == "Grains" }.id
        fun item(price: Double?, date: LocalDate, categoryId: Long?) =
            stockItem(categoryId = categoryId).copy(price = price, purchaseDate = date)

        dao.insertAll(
            listOf(
                item(300.0, LocalDate.of(2026, 11, 1), dairy),
                item(200.0, LocalDate.of(2026, 11, 30), dairy),
                item(900.0, LocalDate.of(2026, 11, 15), grains),
                item(50.0, LocalDate.of(2026, 11, 3), null),
                item(null, LocalDate.of(2026, 11, 3), grains),
                item(999.0, LocalDate.of(2026, 10, 31), dairy),
                item(999.0, LocalDate.of(2026, 12, 1), dairy),
            ),
        )
        val useCase = ObserveMonthlyReportUseCase(ReportRepositoryImpl(dao))

        val report = useCase(YearMonth.of(2026, 11)).first()

        assertEquals(
            listOf(CategorySpending("Grains", 900.0), CategorySpending("Dairy", 500.0), CategorySpending(null, 50.0)),
            report.categories,
        )
        assertEquals(1450.0, report.total, 0.0)
    }
}
