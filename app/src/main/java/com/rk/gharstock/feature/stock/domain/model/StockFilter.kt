package com.rk.gharstock.feature.stock.domain.model

enum class StockSort { NAME, EXPIRY }

data class StockFilter(
    val query: String = "",
    val categoryId: Long? = null,
    val placeId: Long? = null,
    val sort: StockSort = StockSort.NAME,
) {
    val isFiltering: Boolean get() = query.isNotBlank() || categoryId != null || placeId != null
}

private val byName = compareBy<StockItem, String>(String.CASE_INSENSITIVE_ORDER) { it.name }

/** Items without an expiry date go last. */
private val byExpiry = compareBy<StockItem>({ it.expiryDate == null }, { it.expiryDate })
    .then(byName)

fun List<StockItem>.applyFilter(filter: StockFilter): List<StockItem> {
    val query = filter.query.trim()
    return filter { item ->
        (query.isEmpty() || item.name.contains(query, ignoreCase = true)) &&
            (filter.categoryId == null || item.category?.id == filter.categoryId) &&
            (filter.placeId == null || item.place?.id == filter.placeId)
    }.sortedWith(if (filter.sort == StockSort.EXPIRY) byExpiry else byName)
}
