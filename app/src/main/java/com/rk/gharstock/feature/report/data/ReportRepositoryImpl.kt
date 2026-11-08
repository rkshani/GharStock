package com.rk.gharstock.feature.report.data

import com.rk.gharstock.core.database.dao.StockItemDao
import com.rk.gharstock.feature.report.domain.CategorySpending
import com.rk.gharstock.feature.report.domain.ReportRepository
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepositoryImpl @Inject constructor(
    private val stockItemDao: StockItemDao,
) : ReportRepository {

    override fun observeSpendingByCategory(month: YearMonth): Flow<List<CategorySpending>> =
        stockItemDao.observeSpendingByCategory(from = month.atDay(1), until = month.plusMonths(1).atDay(1))
            .map { rows -> rows.map { CategorySpending(it.categoryName, it.total) } }
}
