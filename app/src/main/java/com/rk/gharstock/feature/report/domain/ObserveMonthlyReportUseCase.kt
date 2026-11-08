package com.rk.gharstock.feature.report.domain

import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Grocery spending for one month, from stock item prices and purchase dates. */
class ObserveMonthlyReportUseCase @Inject constructor(
    private val repository: ReportRepository,
) {
    operator fun invoke(month: YearMonth): Flow<MonthlyReport> =
        repository.observeSpendingByCategory(month).map { MonthlyReport(month, it) }
}
