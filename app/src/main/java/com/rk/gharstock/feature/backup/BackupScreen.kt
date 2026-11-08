package com.rk.gharstock.feature.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.UserMessageEffect
import com.rk.gharstock.core.ui.text.formatDate
import com.rk.gharstock.feature.backup.domain.BackupSummary
import java.time.Instant
import java.time.ZoneId

private const val JSON_MIME = "application/json"

// Some file managers report JSON files as plain text or binary.
private val IMPORT_MIME_TYPES = arrayOf(JSON_MIME, "text/plain", "application/octet-stream")

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(JSON_MIME)) { uri ->
        uri?.let { onEvent(BackupEvent.ExportTo(it.toString())) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onEvent(BackupEvent.ImportFrom(it.toString())) }
    }
    BackupContent(
        state = state,
        onEvent = onEvent,
        onBack = onBack,
        onExport = { exportLauncher.launch(state.suggestedFileName) },
        onImport = { importLauncher.launch(IMPORT_MIME_TYPES) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupContent(
    state: BackupUiState,
    onEvent: (BackupEvent) -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(state.message, snackbarHostState) { onEvent(BackupEvent.MessageShown) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isWorking) LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(stringResource(R.string.backup_export_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.backup_export_message), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onExport, enabled = !state.isWorking, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null)
                Text(stringResource(R.string.backup_export_action), modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                stringResource(R.string.backup_import_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(stringResource(R.string.backup_import_message), style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onImport, enabled = !state.isWorking, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.FileUpload, contentDescription = null)
                Text(stringResource(R.string.backup_import_action), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    state.pendingRestore?.let { pending ->
        RestoreConfirmDialog(
            summary = pending.summary,
            onConfirm = { onEvent(BackupEvent.RestoreConfirmed) },
            onDismiss = { onEvent(BackupEvent.RestoreDismissed) },
        )
    }
}

@Composable
private fun RestoreConfirmDialog(summary: BackupSummary, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val date = formatDate(Instant.ofEpochMilli(summary.exportedAt).atZone(ZoneId.systemDefault()).toLocalDate())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_restore_title)) },
        text = {
            Text(
                stringResource(
                    R.string.backup_restore_message,
                    date,
                    summary.stockItems,
                    summary.shoppingItems,
                    summary.medicines,
                    summary.familyMembers,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.backup_restore_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
