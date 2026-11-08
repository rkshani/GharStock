package com.rk.gharstock.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.feature.report.domain.MonthlyReport
import com.rk.gharstock.feature.report.domain.ObserveMonthlyReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ReportUiState(
    val isLoading: Boolean = true,
    val report: MonthlyReport? = null,
    val canGoNext: Boolean = false,
)

sealed interface ReportEvent {
    data object PreviousMonth : ReportEvent
    data object NextMonth : ReportEvent
}

@HiltViewModel
class ReportViewModel @Inject constructor(
    observeMonthlyReport: ObserveMonthlyReportUseCase,
    clock: Clock,
) : ViewModel() {

    private val currentMonth = YearMonth.now(clock)
    private val month = MutableStateFlow(currentMonth)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportUiState> = month
        .flatMapLatest { observeMonthlyReport(it) }
        .map { report -> ReportUiState(isLoading = false, report = report, canGoNext = report.month < currentMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState())

    fun onEvent(event: ReportEvent) {
        when (event) {
            ReportEvent.PreviousMonth -> month.update { it.minusMonths(1) }
            ReportEvent.NextMonth -> month.update { if (it < currentMonth) it.plusMonths(1) else it }
        }
    }
}
