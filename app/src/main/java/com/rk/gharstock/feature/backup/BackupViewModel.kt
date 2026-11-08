package com.rk.gharstock.feature.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.UserMessage
import com.rk.gharstock.feature.backup.domain.ExportBackupUseCase
import com.rk.gharstock.feature.backup.domain.InvalidBackupException
import com.rk.gharstock.feature.backup.domain.PendingRestore
import com.rk.gharstock.feature.backup.domain.ReadBackupUseCase
import com.rk.gharstock.feature.backup.domain.RestoreBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupUiState(
    val suggestedFileName: String = "",
    val isWorking: Boolean = false,
    val pendingRestore: PendingRestore? = null,
    val message: UserMessage? = null,
)

sealed interface BackupEvent {
    data class ExportTo(val uri: String) : BackupEvent
    data class ImportFrom(val uri: String) : BackupEvent
    data object RestoreConfirmed : BackupEvent
    data object RestoreDismissed : BackupEvent
    data object MessageShown : BackupEvent
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackup: ExportBackupUseCase,
    private val readBackup: ReadBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
    clock: Clock,
) : ViewModel() {

    private val state = MutableStateFlow(BackupUiState(suggestedFileName = "ghar-stock-backup-${LocalDate.now(clock)}.json"))
    val uiState: StateFlow<BackupUiState> = state.asStateFlow()

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.ExportTo -> work {
                exportBackup(event.uri)
                    .onSuccess { showMessage(R.string.backup_export_done) }
                    .onFailure { showMessage(R.string.backup_export_failed) }
            }
            is BackupEvent.ImportFrom -> work {
                readBackup(event.uri)
                    .onSuccess { pending -> state.update { it.copy(pendingRestore = pending) } }
                    .onFailure { showMessage(it.importErrorMessage()) }
            }
            BackupEvent.RestoreConfirmed -> {
                val pending = state.value.pendingRestore ?: return
                state.update { it.copy(pendingRestore = null) }
                work {
                    restoreBackup(pending.json)
                        .onSuccess { showMessage(R.string.backup_restore_done) }
                        .onFailure { showMessage(it.importErrorMessage()) }
                }
            }
            BackupEvent.RestoreDismissed -> state.update { it.copy(pendingRestore = null) }
            BackupEvent.MessageShown -> state.update { it.copy(message = null) }
        }
    }

    private fun work(block: suspend () -> Unit) {
        viewModelScope.launch {
            state.update { it.copy(isWorking = true) }
            try {
                block()
            } finally {
                state.update { it.copy(isWorking = false) }
            }
        }
    }

    private fun showMessage(res: Int) {
        state.update { it.copy(message = UserMessage.Text(res)) }
    }

    private fun Throwable.importErrorMessage(): Int =
        if (this is InvalidBackupException) R.string.backup_invalid_file else R.string.backup_read_failed
}
