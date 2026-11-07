package com.rk.gharstock.core.database

import com.rk.gharstock.core.database.entity.DoseScheduleEntity
import com.rk.gharstock.core.database.entity.MedicineEntity
import com.rk.gharstock.core.database.entity.ShoppingItemEntity
import com.rk.gharstock.core.database.entity.StockItemEntity
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import java.time.LocalDate
import java.time.LocalTime

fun shoppingItem(
    listId: Long,
    name: String = "Rice",
    bought: Boolean = false,
    categoryId: Long? = null,
    createdAt: Long = 0,
) = ShoppingItemEntity(
    listId = listId,
    name = name,
    quantity = 1.0,
    unit = MeasureUnit.KG,
    categoryId = categoryId,
    price = null,
    isBought = bought,
    createdAt = createdAt,
)

fun stockItem(
    name: String = "Milk",
    expiryDate: LocalDate? = null,
    categoryId: Long? = null,
    placeId: Long? = null,
) = StockItemEntity(
    name = name,
    quantity = 1.0,
    unit = MeasureUnit.L,
    categoryId = categoryId,
    placeId = placeId,
    price = 300.0,
    purchaseDate = LocalDate.of(2026, 11, 1),
    expiryDate = expiryDate,
    createdAt = 0,
)

fun medicineEntity(memberId: Long, pillsLeft: Double = 10.0) = MedicineEntity(
    memberId = memberId,
    name = "Panadol",
    doseAmount = 2.0,
    pillsLeft = pillsLeft,
    expiryDate = null,
    notes = null,
)

fun fixedTimesSchedule(vararg times: LocalTime) = DoseScheduleEntity(
    medicineId = 0,
    type = ScheduleType.FIXED_TIMES,
    times = times.toList(),
    startTime = null,
    intervalHours = null,
    daysOfWeek = emptySet(),
    startDate = LocalDate.of(2026, 11, 1),
    durationDays = null,
)
