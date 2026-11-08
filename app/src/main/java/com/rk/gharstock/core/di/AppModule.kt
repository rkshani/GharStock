package com.rk.gharstock.core.di

import com.rk.gharstock.core.notification.dose.AlarmDoseReminderScheduler
import com.rk.gharstock.core.notification.dose.DoseNotifier
import com.rk.gharstock.feature.medicine.domain.reminder.DoseReminderScheduler
import com.rk.gharstock.feature.medicine.domain.reminder.MedicineNotifier
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Scope that lives as long as the app process, for work started by receivers or the Application. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppScopeModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Module
@InstallIn(SingletonComponent::class)
interface ReminderModule {
    @Binds
    fun bindDoseReminderScheduler(impl: AlarmDoseReminderScheduler): DoseReminderScheduler

    @Binds
    fun bindMedicineNotifier(impl: DoseNotifier): MedicineNotifier
}
