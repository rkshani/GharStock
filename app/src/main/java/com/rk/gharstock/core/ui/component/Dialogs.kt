package com.rk.gharstock.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.rk.gharstock.R

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String = stringResource(R.string.delete_message),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_title, itemName)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/** State of a simple "enter a name" dialog, kept in the ViewModel. */
data class NameDialogState(
    val id: Long = 0,
    val text: String = "",
    val secondaryText: String = "",
    @StringRes val error: Int? = null,
) {
    val isNew: Boolean get() = id == 0L
}

/** Dialog with one name field and an optional second field (e.g. relation). */
@Composable
fun NameInputDialog(
    title: String,
    state: NameDialogState,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    label: String = stringResource(R.string.field_name),
    secondaryLabel: String? = null,
    onSecondaryTextChange: (String) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.text,
                    onValueChange = onTextChange,
                    label = { Text(label) },
                    singleLine = true,
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(stringResource(it)) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = if (secondaryLabel == null) ImeAction.Done else ImeAction.Next,
                    ),
                    modifier = Modifier.focusRequester(focusRequester),
                )
                if (secondaryLabel != null) {
                    OutlinedTextField(
                        value = state.secondaryText,
                        onValueChange = onSecondaryTextChange,
                        label = { Text(secondaryLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
