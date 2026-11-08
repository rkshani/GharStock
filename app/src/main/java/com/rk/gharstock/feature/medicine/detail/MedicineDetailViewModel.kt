package com.rk.gharstock.feature.medicine.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rk.gharstock.feature.medicine.MedicineDetailRoute
import com.rk.gharstock.feature.medicine.domain.FamilyMemberRepository
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.DoseLog
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.usecase.DeleteMedicineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicineDetailUiState(
    val isLoading: Boolean = true,
    val medicine: Medicine? = null,
    val memberName: String = "",
    val today: LocalDate = LocalDate.MIN,
    val logs: List<DoseLog> = emptyList(),
    val showDeleteConfirm: Boolean = false,
) {
    /** Medicine was deleted (here or elsewhere) after loading. */
    val isGone: Boolean get() = !isLoading && medicine == null
}

sealed interface MedicineDetailEvent {
    data object DeleteClicked : MedicineDetailEvent
    data object DeleteConfirmed : MedicineDetailEvent
    data object DeleteDismissed : MedicineDetailEvent
}

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    medicineRepository: MedicineRepository,
    memberRepository: FamilyMemberRepository,
    private val deleteMedicine: DeleteMedicineUseCase,
    clock: Clock,
) : ViewModel() {

    private val medicineId = savedStateHandle.toRoute<MedicineDetailRoute>().medicineId
    private val showDeleteConfirm = MutableStateFlow(false)

    val uiState: StateFlow<MedicineDetailUiState> = combine(
        medicineRepository.observeMedicine(medicineId),
        medicineRepository.observeDoseLogs(medicineId),
        memberRepository.observeMembers(),
        showDeleteConfirm,
    ) { medicine, logs, members, confirm ->
        MedicineDetailUiState(
            isLoading = false,
            medicine = medicine,
            memberName = members.firstOrNull { it.id == medicine?.memberId }?.name.orEmpty(),
            today = LocalDate.now(clock),
            logs = logs,
            showDeleteConfirm = confirm,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicineDetailUiState())

    fun onEvent(event: MedicineDetailEvent) {
        when (event) {
            MedicineDetailEvent.DeleteClicked -> showDeleteConfirm.update { true }
            MedicineDetailEvent.DeleteConfirmed -> {
                showDeleteConfirm.update { false }
                viewModelScope.launch { deleteMedicine(medicineId) }
            }
            MedicineDetailEvent.DeleteDismissed -> showDeleteConfirm.update { false }
        }
    }
}
