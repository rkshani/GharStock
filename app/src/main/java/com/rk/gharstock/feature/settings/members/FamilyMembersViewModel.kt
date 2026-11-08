package com.rk.gharstock.feature.settings.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.core.ui.component.NameDialogState
import com.rk.gharstock.core.ui.text.validationMessageRes
import com.rk.gharstock.feature.medicine.domain.FamilyMemberRepository
import com.rk.gharstock.feature.medicine.domain.model.FamilyMember
import com.rk.gharstock.feature.medicine.domain.usecase.DeleteFamilyMemberUseCase
import com.rk.gharstock.feature.medicine.domain.usecase.SaveFamilyMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FamilyMembersUiState(
    val isLoading: Boolean = true,
    val members: List<FamilyMember> = emptyList(),
    val dialog: NameDialogState? = null,
    val deleteTarget: FamilyMember? = null,
)

sealed interface FamilyMembersEvent {
    data object AddClicked : FamilyMembersEvent
    data class EditClicked(val member: FamilyMember) : FamilyMembersEvent
    data class NameChanged(val text: String) : FamilyMembersEvent
    data class RelationChanged(val text: String) : FamilyMembersEvent
    data object DialogConfirmed : FamilyMembersEvent
    data object DialogDismissed : FamilyMembersEvent
    data class DeleteClicked(val member: FamilyMember) : FamilyMembersEvent
    data object DeleteConfirmed : FamilyMembersEvent
    data object DeleteDismissed : FamilyMembersEvent
}

private data class MemberDialogs(
    val dialog: NameDialogState? = null,
    val deleteTarget: FamilyMember? = null,
)

@HiltViewModel
class FamilyMembersViewModel @Inject constructor(
    repository: FamilyMemberRepository,
    private val saveMember: SaveFamilyMemberUseCase,
    private val deleteMember: DeleteFamilyMemberUseCase,
) : ViewModel() {

    private val dialogs = MutableStateFlow(MemberDialogs())

    val uiState: StateFlow<FamilyMembersUiState> = combine(repository.observeMembers(), dialogs) { members, dialogs ->
        FamilyMembersUiState(
            isLoading = false,
            members = members,
            dialog = dialogs.dialog,
            deleteTarget = dialogs.deleteTarget,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyMembersUiState())

    fun onEvent(event: FamilyMembersEvent) {
        when (event) {
            FamilyMembersEvent.AddClicked -> dialogs.update { it.copy(dialog = NameDialogState()) }
            is FamilyMembersEvent.EditClicked -> dialogs.update {
                it.copy(
                    dialog = NameDialogState(
                        id = event.member.id,
                        text = event.member.name,
                        secondaryText = event.member.relation.orEmpty(),
                    ),
                )
            }
            is FamilyMembersEvent.NameChanged -> dialogs.update {
                it.copy(dialog = it.dialog?.copy(text = event.text, error = null))
            }
            is FamilyMembersEvent.RelationChanged -> dialogs.update {
                it.copy(dialog = it.dialog?.copy(secondaryText = event.text))
            }
            FamilyMembersEvent.DialogConfirmed -> save()
            FamilyMembersEvent.DialogDismissed -> dialogs.update { it.copy(dialog = null) }
            is FamilyMembersEvent.DeleteClicked -> dialogs.update { it.copy(deleteTarget = event.member) }
            FamilyMembersEvent.DeleteConfirmed -> delete()
            FamilyMembersEvent.DeleteDismissed -> dialogs.update { it.copy(deleteTarget = null) }
        }
    }

    private fun save() {
        val dialog = dialogs.value.dialog ?: return
        viewModelScope.launch {
            saveMember(FamilyMember(id = dialog.id, name = dialog.text, relation = dialog.secondaryText))
                .onSuccess { dialogs.update { it.copy(dialog = null) } }
                .onFailure { error ->
                    dialogs.update { it.copy(dialog = dialog.copy(error = error.validationMessageRes())) }
                }
        }
    }

    private fun delete() {
        val target = dialogs.value.deleteTarget ?: return
        dialogs.update { it.copy(deleteTarget = null) }
        viewModelScope.launch { deleteMember(target.id) }
    }
}
