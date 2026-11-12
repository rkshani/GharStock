package com.rk.gharstock.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.R
import com.rk.gharstock.core.domain.expiry.ExpiryAlert
import com.rk.gharstock.core.domain.expiry.ObserveExpiryAlertsUseCase
import com.rk.gharstock.core.ui.component.UserMessage
import com.rk.gharstock.feature.medicine.domain.model.DoseStatus
import com.rk.gharstock.feature.medicine.domain.usecase.LogDoseUseCase
import com.rk.gharstock.feature.medicine.domain.usecase.ObserveTodayDosesUseCase
import com.rk.gharstock.feature.medicine.domain.usecase.TodayDose
import com.rk.gharstock.feature.shopping.domain.ShoppingRepository
import com.rk.gharstock.feature.stock.domain.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.MIN,
    val stockCount: Int = 0,
    val pendingShoppingCount: Int = 0,
    val todayDoses: List<TodayDose> = emptyList(),
    val expiring: List<ExpiryAlert> = emptyList(),
    /** In-app alert, e.g. when a taken dose leaves a medicine low on pills. */
    val message: UserMessage? = null,
)

sealed interface HomeEvent {
    data class DoseTaken(val dose: TodayDose) : HomeEvent
    data class DoseSkipped(val dose: TodayDose) : HomeEvent
    data object MessageShown : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    stockRepository: StockRepository,
    shoppingRepository: ShoppingRepository,
    observeExpiryAlerts: ObserveExpiryAlertsUseCase,
    observeTodayDoses: ObserveTodayDosesUseCase,
    private val logDose: LogDoseUseCase,
    clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)
    private val userMessage = MutableStateFlow<UserMessage?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        stockRepository.observeCount(),
        shoppingRepository.observePendingCount(),
        observeTodayDoses(today),
        observeExpiryAlerts(today),
        userMessage,
    ) { stockCount, pendingCount, doses, expiring, message ->
        HomeUiState(
            isLoading = false,
            today = today,
            stockCount = stockCount,
            pendingShoppingCount = pendingCount,
            todayDoses = doses,
            expiring = expiring,
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.DoseTaken -> log(event.dose, DoseStatus.TAKEN)
            is HomeEvent.DoseSkipped -> log(event.dose, DoseStatus.SKIPPED)
            HomeEvent.MessageShown -> userMessage.value = null
        }
    }

    private fun log(dose: TodayDose, status: DoseStatus) {
        viewModelScope.launch {
            val result = logDose(dose.medicine.id, dose.scheduledAt, status)
            if (result.becameLowOnPills) {
                userMessage.value = UserMessage.Text(R.string.home_low_pills, dose.medicine.name)
            }
        }
    }
}
