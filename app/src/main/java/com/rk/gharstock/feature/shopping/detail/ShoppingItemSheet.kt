package com.rk.gharstock.feature.shopping.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rk.gharstock.R
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.ui.component.CategoryDropdown
import com.rk.gharstock.core.ui.component.UnitDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemSheet(
    form: ShoppingItemForm,
    categories: List<Category>,
    onChange: (ShoppingItemForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(if (form.isNew) R.string.shopping_add_item else R.string.shopping_edit_item),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = form.name,
                onValueChange = { onChange(form.copy(name = it)) },
                label = { Text(stringResource(R.string.field_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.quantity,
                    onValueChange = { onChange(form.copy(quantity = it)) },
                    label = { Text(stringResource(R.string.field_quantity)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                UnitDropdown(
                    selected = form.unit,
                    onSelect = { onChange(form.copy(unit = it)) },
                    modifier = Modifier.weight(1f),
                )
            }
            CategoryDropdown(
                categories = categories,
                selected = form.category,
                onSelect = { onChange(form.copy(category = it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.price,
                onValueChange = { onChange(form.copy(price = it)) },
                label = { Text(stringResource(R.string.field_price)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            form.error?.let {
                Text(stringResource(it), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
