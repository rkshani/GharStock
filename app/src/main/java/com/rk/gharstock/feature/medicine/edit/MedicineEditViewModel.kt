package com.rk.gharstock.feature.medicine.edit

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rk.gharstock.core.ui.text.formatEditableNumber
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.core.util.toDecimalOrNull
import com.rk.gharstock.feature.medicine.MedicineEditRoute
import com.rk.gharstock.feature.medicine.domain.FamilyMemberRepository
import com.rk.gharstock.feature.medicine.domain.MedicineRepository
import com.rk.gharstock.feature.medicine.domain.model.DoseSchedule
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.model.Medicine
import com.rk.gharstock.feature.medicine.domain.model.ScheduleType
import com.rk.gharstock.feature.medicine.domain.usecase.SaveMedicineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which picker dialog is open on the edit screen. */
enum class MedicinePicker { ADD_TIME, START_TIME, START_DATE, EXPIRY_DATE }

/** Editable medicine fields; numbers are kept as typed text. */
data class MedicineForm(
    val memberId: Long = 0,
    val name: String = "",
    val doseAmount: String = "1",
    val pillsLeft: String = "",
    val expiryDate: LocalDate? = null,
    val notes: String = "",
    val type: ScheduleType = ScheduleType.FIXED_TIMES,
    val times: List<LocalTime> = emptyList(),
    val startTime: LocalTime? = null,
    val intervalHours: String = "8",
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val startDate: LocalDate,
    val durationDays: String = "",
)

data class MedicineEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val members: List<FamilyMember> = emptyList(),
    val form: MedicineForm = MedicineForm(startDate = LocalDate.MIN),
    val picker: MedicinePicker? = null,
    @StringRes val error: Int? = null,
    val isFinished: Boolean = false,
)

sealed interface MedicineEditEvent {
    data class FormChanged(val form: MedicineForm) : MedicineEditEvent
    data class OpenPicker(val picker: MedicinePicker) : MedicineEditEvent
    data object PickerDismissed : MedicineEditEvent
    data class TimePicked(val time: LocalTime) : MedicineEditEvent
    data class DatePicked(val date: LocalDate) : MedicineEditEvent
    data class RemoveTime(val time: LocalTime) : MedicineEditEvent
    data class DayToggled(val day: DayOfWeek) : MedicineEditEvent
    data object Save : MedicineEditEvent
}

private data class MedicineEditLocal(
    val isLoading: Boolean,
    val form: MedicineForm,
    val picker: MedicinePicker? = null,
    @StringRes val error: Int? = null,
    val isFinished: Boolean = false,
)

@HiltViewModel
class MedicineEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicineRepository: MedicineRepository,
    private val memberRepository: FamilyMemberRepository,
    private val saveMedicine: SaveMedicineUseCase,
    clock: Clock,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<MedicineEditRoute>()
    private val local = MutableStateFlow(
        MedicineEditLocal(
            isLoading = route.medicineId != 0L,
            form = MedicineForm(memberId = route.memberId, startDate = LocalDate.now(clock)),
        ),
    )

    val uiState: StateFlow<MedicineEditUiState> =
        combine(local, memberRepository.observeMembers()) { local, members ->
            MedicineEditUiState(
                isLoading = local.isLoading,
                isNew = route.medicineId == 0L,
                members = members,
                form = local.form,
                picker = local.picker,
                error = local.error,
                isFinished = local.isFinished,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicineEditUiState())

    init {
        if (route.medicineId != 0L) load() else preselectOnlyMember()
    }

    fun onEvent(event: MedicineEditEvent) {
        when (event) {
            is MedicineEditEvent.FormChanged -> updateForm { event.form }
            is MedicineEditEvent.OpenPicker -> local.update { it.copy(picker = event.picker) }
            MedicineEditEvent.PickerDismissed -> local.update { it.copy(picker = null) }
            is MedicineEditEvent.TimePicked -> onTimePicked(event.time)
            is MedicineEditEvent.DatePicked -> onDatePicked(event.date)
            is MedicineEditEvent.RemoveTime -> updateForm { it.copy(times = it.times - event.time) }
            is MedicineEditEvent.DayToggled -> updateForm {
                val days = if (event.day in it.daysOfWeek) it.daysOfWeek - event.day else it.daysOfWeek + event.day
                it.copy(daysOfWeek = days)
            }
            MedicineEditEvent.Save -> save()
        }
    }

    private fun updateForm(transform: (MedicineForm) -> MedicineForm) {
        local.update { it.copy(form = transform(it.form), error = null) }
    }

    private fun onTimePicked(time: LocalTime) {
        val picker = local.value.picker
        local.update { it.copy(picker = null) }
        when (picker) {
            MedicinePicker.ADD_TIME -> updateForm { it.copy(times = (it.times + time).distinct().sorted()) }
            MedicinePicker.START_TIME -> updateForm { it.copy(startTime = time) }
            else -> Unit
        }
    }

    private fun onDatePicked(date: LocalDate) {
        val picker = local.value.picker
        local.update { it.copy(picker = null) }
        when (picker) {
            MedicinePicker.START_DATE -> updateForm { it.copy(startDate = date) }
            MedicinePicker.EXPIRY_DATE -> updateForm { it.copy(expiryDate = date) }
            else -> Unit
        }
    }

    /** With only one family member, there is nothing to choose. */
    private fun preselectOnlyMember() {
        if (route.memberId != 0L) return
        viewModelScope.launch {
            val only = memberRepository.observeMembers().first().singleOrNull() ?: return@launch
            local.update { state ->
                if (state.form.memberId == 0L) state.copy(form = state.form.copy(memberId = only.id)) else state
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val medicine = medicineRepository.getMedicine(route.medicineId)
            local.update {
                if (medicine == null) it.copy(isFinished = true) else it.copy(isLoading = false, form = medicine.toForm())
            }
        }
    }

    private fun save() {
        val form = local.value.form
        viewModelScope.launch {
            saveMedicine(form.toMedicine(route.medicineId))
                .onSuccess { local.update { it.copy(isFinished = true) } }
                .onFailure { error -> local.update { it.copy(error = error.validationMessageRes()) } }
        }
    }
}

/** Builds the domain medicine, keeping only the schedule fields its type uses. */
internal fun MedicineForm.toMedicine(id: Long): Medicine {
    val usesTimes = type != ScheduleType.EVERY_X_HOURS
    val schedule = DoseSchedule(
        type = type,
        times = if (usesTimes) times else emptyList(),
        startTime = startTime.takeIf { type == ScheduleType.EVERY_X_HOURS },
        intervalHours = intervalHours.trim().toIntOrNull().takeIf { type == ScheduleType.EVERY_X_HOURS },
        daysOfWeek = if (type == ScheduleType.DAYS_OF_WEEK) daysOfWeek else emptySet(),
        startDate = startDate,
        durationDays = if (type == ScheduleType.COURSE) durationDays.trim().toIntOrNull() ?: 0 else null,
    )
    return Medicine(
        id = id,
        memberId = memberId,
        name = name,
        doseAmount = doseAmount.toDecimalOrNull() ?: Double.NaN,
        pillsLeft = pillsLeft.toDecimalOrNull() ?: Double.NaN,
        expiryDate = expiryDate,
        notes = notes,
        schedule = schedule,
    )
}

private fun Medicine.toForm() = MedicineForm(
    memberId = memberId,
    name = name,
    doseAmount = formatEditableNumber(doseAmount),
    pillsLeft = formatEditableNumber(pillsLeft),
    expiryDate = expiryDate,
    notes = notes.orEmpty(),
    type = schedule.type,
    times = schedule.times,
    startTime = schedule.startTime,
    intervalHours = schedule.intervalHours?.toString() ?: "8",
    daysOfWeek = schedule.daysOfWeek,
    startDate = schedule.startDate,
    durationDays = schedule.durationDays?.toString().orEmpty(),
)
