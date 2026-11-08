package com.rk.gharstock.feature.medicine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.MemberWithMedicines
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MedicineListUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.MIN,
    val groups: List<MemberWithMedicines> = emptyList(),
) {
    val hasMembers: Boolean get() = groups.isNotEmpty()
    val hasMedicines: Boolean get() = groups.any { it.medicines.isNotEmpty() }
}

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    repository: MedicineRepository,
    clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<MedicineListUiState> = repository.observeMedicinesByMember()
        .map { groups ->
            MedicineListUiState(
                isLoading = false,
                today = LocalDate.now(clock),
                groups = groups.map { group -> group.copy(medicines = group.medicines.sortedBy { it.name.lowercase() }) },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicineListUiState())
}
