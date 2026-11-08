package com.rk.gharstock.feature.stock.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rk.gharstock.R
import com.rk.gharstock.feature.shopping.domain.model.ShoppingList

@Composable
fun AddToShoppingListDialog(
    dialog: AddToListDialogState,
    lists: List<ShoppingList>,
    onChange: (AddToListDialogState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stock_add_to_shopping)) },
        text = {
            if (lists.isEmpty()) {
                Text(stringResource(R.string.stock_no_lists))
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(dialog.item.name, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = dialog.quantity,
                        onValueChange = { onChange(dialog.copy(quantity = it)) },
                        label = { Text(stringResource(R.string.field_quantity)) },
                        singleLine = true,
                        isError = dialog.error != null,
                        supportingText = dialog.error?.let { { Text(stringResource(it)) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Text(stringResource(R.string.stock_choose_list), style = MaterialTheme.typography.labelLarge)
                    lists.forEach { list ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = list.id == dialog.selectedListId,
                                    onClick = { onChange(dialog.copy(selectedListId = list.id)) },
                                    role = Role.RadioButton,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = list.id == dialog.selectedListId, onClick = null)
                            Text(list.name, modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (lists.isEmpty()) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_ok)) }
            } else {
                TextButton(onClick = onConfirm, enabled = dialog.selectedListId != null) {
                    Text(stringResource(R.string.action_add))
                }
            }
        },
        dismissButton = {
            if (lists.isNotEmpty()) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            }
        },
    )
}
