package com.rk.gharstock.feature.report.domain

import java.time.YearMonth

/** Spending in one category; [categoryName] is null for uncategorized items. */
data class CategorySpending(
    val categoryName: String?,
    val total: Double,
)

data class MonthlyReport(
    val month: YearMonth,
    val categories: List<CategorySpending>,
) {
    val total: Double get() = categories.sumOf { it.total }
}
