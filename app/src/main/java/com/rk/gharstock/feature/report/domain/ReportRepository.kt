package com.rk.gharstock.feature.report.domain

import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    /** Total price of stock items purchased in [month], per category, highest first. */
    fun observeSpendingByCategory(month: YearMonth): Flow<List<CategorySpending>>
}
