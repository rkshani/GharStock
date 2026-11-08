package com.rk.gharstock.core.di

import com.rk.gharstock.core.database.RoomTransactionRunner
import com.rk.gharstock.core.domain.TransactionRunner
import com.rk.gharstock.feature.backup.data.BackupRepositoryImpl
import com.rk.gharstock.feature.backup.data.ContentResolverDocumentStore
import com.rk.gharstock.feature.backup.domain.BackupRepository
import com.rk.gharstock.feature.backup.domain.DocumentStore
import com.rk.gharstock.feature.medicine.data.FamilyMemberRepositoryImpl
import com.rk.gharstock.feature.medicine.data.MedicineRepositoryImpl
import com.rk.gharstock.feature.medicine.domain.FamilyMemberRepository
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.report.data.ReportRepositoryImpl
import com.rk.gharstock.feature.report.domain.ReportRepository
import com.rk.gharstock.feature.settings.data.CatalogRepositoryImpl
import com.rk.gharstock.feature.settings.domain.CatalogRepository
import com.rk.gharstock.feature.shopping.data.ShoppingRepositoryImpl
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.stock.data.StockRepositoryImpl
import com.rk.gharstock.feature.stock.domain.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindTransactionRunner(impl: RoomTransactionRunner): TransactionRunner

    @Binds
    fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository

    @Binds
    fun bindStockRepository(impl: StockRepositoryImpl): StockRepository

    @Binds
    fun bindFamilyMemberRepository(impl: FamilyMemberRepositoryImpl): FamilyMemberRepository

    @Binds
    fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds
    fun bindDocumentStore(impl: ContentResolverDocumentStore): DocumentStore

    @Binds
    fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    fun bindMedicineRepository(impl: MedicineRepositoryImpl): MedicineRepository
}
