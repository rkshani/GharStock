package com.rk.gharstock.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rk.gharstock.core.domain.TransactionRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

/** Base class for tests that need a real (in-memory) Room database with seeded defaults. */
@RunWith(AndroidJUnit4::class)
abstract class DatabaseTest {

    protected lateinit var db: GharStockDatabase

    protected val clock: Clock = Clock.fixed(Instant.parse("2026-11-08T04:00:00Z"), ZoneOffset.UTC)
    protected val today: LocalDate = LocalDate.now(clock)

    protected val transaction = object : TransactionRunner {
        override suspend fun <T> invoke(block: suspend () -> T): T = db.withTransaction(block)
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GharStockDatabase::class.java)
            .addCallback(SeedDefaultsCallback(context))
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }
}
