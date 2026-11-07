package com.rk.gharstock.core.di

import android.content.Context
import androidx.room.Room
import com.rk.gharstock.core.database.GharStockDatabase
import com.rk.gharstock.core.database.SeedDefaultsCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GharStockDatabase =
        Room.databaseBuilder(context, GharStockDatabase::class.java, GharStockDatabase.NAME)
            .addCallback(SeedDefaultsCallback(context))
            .build()

    @Provides
    fun provideCategoryDao(db: GharStockDatabase) = db.categoryDao()

    @Provides
    fun provideStoragePlaceDao(db: GharStockDatabase) = db.storagePlaceDao()

    @Provides
    fun provideShoppingListDao(db: GharStockDatabase) = db.shoppingListDao()

    @Provides
    fun provideShoppingItemDao(db: GharStockDatabase) = db.shoppingItemDao()

    @Provides
    fun provideStockItemDao(db: GharStockDatabase) = db.stockItemDao()

    @Provides
    fun provideFamilyMemberDao(db: GharStockDatabase) = db.familyMemberDao()

    @Provides
    fun provideMedicineDao(db: GharStockDatabase) = db.medicineDao()

    @Provides
    fun provideDoseLogDao(db: GharStockDatabase) = db.doseLogDao()

    /** System clock in the device time zone. Injected so use cases can be tested with a fixed clock. */
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
