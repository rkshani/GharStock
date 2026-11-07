package com.rk.gharstock.core.database

import androidx.room.withTransaction
import com.rk.gharstock.core.domain.TransactionRunner
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val database: GharStockDatabase,
) : TransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T = database.withTransaction(block)
}
